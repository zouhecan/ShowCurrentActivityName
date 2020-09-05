package com.example.permission;

import java.util.List;

public interface IPermissionCallBack {
    void onPermissionsGranted(boolean grantedAll, List<String> perms);
}
