package com.example.showcurrentactivityname;

import java.util.List;

public interface IPermissionCallBack {
    void onPermissionsGranted(boolean grantedAll, List<String> perms);
}
