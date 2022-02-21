package com.tang.draw.page.slice;

import com.tang.draw.Constants;
import com.tang.draw.ResourceTable;
import com.tang.draw.dialog.AlertDialog;
import com.tang.draw.dialog.ConfirmDialog;
import com.tang.draw.dialog.PromptDialog;
import com.tang.draw.provide.KeyItemProvider;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.utils.ToastUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.utils.net.Uri;

import java.util.ArrayList;
import java.util.List;

/********
 *文件名: DataAbilitySlice
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/15 16:39
 *描述: DataAbilitySlice
 ********/
public class DataAbilitySlice  extends AbilitySlice {

    private DataAbilityHelper databaseHelper;

    private Button backBtn;
    private Image addBtn;
    private TextField search;
    private ListContainer dataList;
    private List<String> keysList;
    private ConfirmDialog confirmDialog;
    private PromptDialog promptDialog;
    private KeyItemProvider keyItemProvider;
    private int deleteIndex=-1;


    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_data);
        initDatabaseHelper();
        initComponents();
        initClickedListener();
        initDialog();

    }

    private void initDatabaseHelper() {
        databaseHelper = DataAbilityHelper.creator(this);
    }

    private void initClickedListener() {
        addBtn.setClickedListener(this::addKey);
        backBtn.setClickedListener(component -> terminate());

        initKeyList();
        keyItemProvider=new KeyItemProvider(this,keysList);

        dataList.setItemProvider(keyItemProvider);
        dataList.setItemLongClickedListener(new ListContainer.ItemLongClickedListener() {
            @Override
            public boolean onItemLongClicked(ListContainer listContainer, Component component, int i, long l) {
                confirmDialog.setDetailText("确定删除"+keyItemProvider.getItem(i)+"?");
                deleteIndex=i;
                confirmDialog.show();
                return false;
            }
        });

        search.addTextObserver(new Text.TextObserver() {
            @Override
            public void onTextUpdated(String s, int i, int i1, int i2) {
                keysList.clear();
                keysList.addAll(fuzzyQuery(s));
                keyItemProvider.notifyDataChanged();

            }
        });
    }

    private void initComponents() {
        backBtn = findComponentById(ResourceTable.Id_btn_back);
        addBtn=findComponentById(ResourceTable.Id_btn_add);
        dataList=findComponentById(ResourceTable.Id_list_data);
        search=findComponentById(ResourceTable.Id_field_search);

    }

    private void initKeyList(){
        this.keysList=fuzzyQuery("");
    }

    private void initDialog(){
        confirmDialog=new ConfirmDialog(this);
        confirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
            @Override
            public void onOKClick() {
                if(deleteIndex!=-1){
                    delete(keysList.get(deleteIndex));
                    ToastUtils.show(getContext(),"删除成功");
                    refreshKeyList();
                }

            }

            @Override
            public void onCancelClick() {

            }
        });

        promptDialog=new PromptDialog(this);
        promptDialog.setOnDialogClickListener(new PromptDialog.DialogClickListener() {
            @Override
            public void onOKClick(String inputData) {
                if(query(inputData)){
                    insert(inputData);
                    refreshKeyList();
                    promptDialog.hide();
                    ToastUtils.show(getContext(),"添加成功");
                }

                else {
                    promptDialog.setError(true,"已存在该数据");
                }
            }

            @Override
            public void onCancelClick() {

            }
        });
    }

    /**
     * 点击添加按钮后弹出输入框
     *
     * @param component
     */
    private void addKey(Component component){
        promptDialog.show();
    }

    /**
     * 插入一条数据
     *
     * @param value
     */
    private int insert(String value) {

        ValuesBucket valuesBucket = new ValuesBucket();
        valuesBucket.putString(Constants.DB_COLUMN_KEY, value);
        try {
           return databaseHelper.insert(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), valuesBucket);
        } catch (DataAbilityRemoteException | IllegalStateException exception) {
            LogUtils.error("insert: dataRemote exception|illegalStateException"+exception.getMessage());
        }
        return -1;
    }


    /**
     * 删除一条数据
     * @param value
     * @return
     */
    private int delete(String value){

        DataAbilityPredicates predicates = new DataAbilityPredicates();
        predicates.equalTo(Constants.DB_COLUMN_KEY,value);

        try {
           return databaseHelper.delete(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), predicates);
        } catch (DataAbilityRemoteException | IllegalStateException exception) {
            LogUtils.error("delete: dataRemote exception|illegalStateException"+exception.getMessage());
        }
        return 0;
    }

    /**
     * 模糊查询
     *
     * @param key
     * @return
     */
    private List<String> fuzzyQuery(String key) {

        String[] columns = new String[]{Constants.DB_COLUMN_KEY};
        List<String> result = new ArrayList<>();

        DataAbilityPredicates predicates = new DataAbilityPredicates();
        predicates.contains(Constants.DB_COLUMN_KEY,key);
        try {
            ResultSet resultSet = databaseHelper.query(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), columns,
                    predicates);
            if (!resultSet.goToFirstRow())
                return null;
            int index=resultSet.getColumnIndexForName(Constants.DB_COLUMN_KEY);

            do {
                String tmp = resultSet.getString(index);
                result.add(tmp);
            } while (resultSet.goToNextRow());
            resultSet.close();

        } catch (DataAbilityRemoteException | IllegalStateException exception) {
            LogUtils.error( "fuzzyQuery: dataRemote exception|illegalStateException"+exception.getMessage());
        }

        return result;
    }

    /**
     * 查询是否存在指定数据
     *
     * @param value
     * @return
     */
    private boolean query(String value){
        String[] columns = new String[]{Constants.DB_COLUMN_KEY};
        DataAbilityPredicates predicates = new DataAbilityPredicates();
        predicates.equalTo(Constants.DB_COLUMN_KEY,value);
        try {
            ResultSet resultSet = databaseHelper.query(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), columns,
                    predicates);
            if (!resultSet.goToFirstRow())
                return true;
            if(resultSet.getRowCount()>=1){
                return false;
            }

        } catch (DataAbilityRemoteException | IllegalStateException exception) {
            LogUtils.error( "query: dataRemote exception|illegalStateException"+exception.getMessage());
        }
        return false;
    }

    /**
     * 刷新列表
     *
     */
    private void refreshKeyList(){
        this.keysList.clear();
        this.keysList.addAll(fuzzyQuery(""));
        keyItemProvider.notifyDataChanged();
    }
}
