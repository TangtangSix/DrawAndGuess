package com.tang.draw.page;

import com.tang.draw.Constants;
import com.tang.draw.utils.LogUtils;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.data.DatabaseHelper;
import ohos.data.dataability.DataAbilityUtils;
import ohos.data.rdb.*;
import ohos.data.resultset.ResultSet;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.net.Uri;
import ohos.utils.PacMap;

import java.io.FileDescriptor;

public class DataAbility extends Ability {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "Demo");

    private final StoreConfig config = StoreConfig.newDefaultConfig(Constants.DB_NAME);

    private RdbStore rdbStore;

    private final RdbOpenCallback rdbOpenCallback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore store) {
            store.executeSql(
                    "create table if not exists " + Constants.DB_TAB_NAME + " (userId integer primary key autoincrement, "
                            + Constants.DB_COLUMN_KEY + " text not null)");

            LogUtils.info( "create a  new database");
        }

        @Override
        public void onUpgrade(RdbStore store, int oldVersion, int newVersion) {
            LogUtils.info( "DataBase upgrade");
        }
    };
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        rdbStore = databaseHelper.getRdbStore(config, 1, rdbOpenCallback, null);
        insertPreSetKey();
    }

    @Override
    public ResultSet query(Uri uri, String[] columns, DataAbilityPredicates predicates) {
        RdbPredicates rdbPredicates = DataAbilityUtils.createRdbPredicates(predicates, Constants.DB_TAB_NAME);
        return rdbStore.query(rdbPredicates, columns);
    }

    @Override
    public int insert(Uri uri, ValuesBucket value) {
        String path = uri.getLastPath();
        if (!"draw".equals(path)) {
            LogUtils.info( "DataAbility insert path is not matched");
            return -1;
        }

        ValuesBucket values = new ValuesBucket();
        values.putString(Constants.DB_COLUMN_KEY, value.getString(Constants.DB_COLUMN_KEY));
        int index = (int) rdbStore.insert(Constants.DB_TAB_NAME, values);
        DataAbilityHelper.creator(this).notifyChange(uri);
        return index;
    }

    @Override
    public int delete(Uri uri, DataAbilityPredicates predicates) {

        RdbPredicates rdbPredicates = DataAbilityUtils.createRdbPredicates(predicates, Constants.DB_TAB_NAME);
        int index = rdbStore.delete(rdbPredicates);
        HiLog.info(LABEL_LOG, "%{public}s", "delete");
        DataAbilityHelper.creator(this).notifyChange(uri);
        return index;
    }

    @Override
    public int update(Uri uri, ValuesBucket value, DataAbilityPredicates predicates) {
        return 0;
    }

    @Override
    public FileDescriptor openFile(Uri uri, String mode) {
        return null;
    }

    @Override
    public String[] getFileTypes(Uri uri, String mimeTypeFilter) {
        return new String[0];
    }

    @Override
    public PacMap call(String method, String arg, PacMap extras) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }


    private void insertPreSetKey(){
        ValuesBucket[] values = new ValuesBucket[Constants.PRESET_KEY.length];
        for (int i = 0; i < Constants.PRESET_KEY.length; i++) {
            values[i]=new ValuesBucket();
            values[i].putString(Constants.DB_COLUMN_KEY, Constants.PRESET_KEY[i]);
        }
        try {
            DataAbilityHelper databaseHelper = DataAbilityHelper.creator(this);
            databaseHelper.batchInsert(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), values);
        } catch (DataAbilityRemoteException | IllegalStateException | NullPointerException exception) {
            LogUtils.error(getClass().getSimpleName()+" insertPreSetKey: dataRemote exception|illegalStateException"+exception.getMessage());
        }
    }
}