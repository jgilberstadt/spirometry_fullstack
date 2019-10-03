package com.spirometry.homespirometry;

/**
 * Created by dingwenli on 1/17/18.
 */

public class UrlConfig {
    // Server upload url
    static String URL_FVC_UPLOAD = "http://172.16.10.165/spirometry/test_fvc_upload.php";
    static String URL_PEFFEV1_UPLOAD = "http://172.16.10.165/spirometry/test_peffev1_upload.php";
    static String URL_VITOL_UPLOAD = "http://172.16.10.165/spirometry/vitol_upload.php";
    static String URL_CHECK_FILE_EXIST = "https://ecp-app-hsdev1.nrg.wustl.edu/check_upload.php";
    static String URL_CHECK_PATIENT_EXIST = "https://ecp-app-hsdev1.nrg.wustl.edu/spirometry/login.php";
}
