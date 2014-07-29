package com.elvizlai.h9.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.elvizlai.h9.R;
import com.elvizlai.h9.config.Config;
import com.elvizlai.h9.test.Test;
import com.elvizlai.h9.util.Encrypt;
import com.elvizlai.h9.util.ToastUtil;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elvizlai on 14-4-16.
 * 本类托管登录界面
 */
public class Login extends BaseActivity {
    private EditText ed_account, ed_password;
    private CheckBox cb_isRempsw, cb_isAutologin;
    private Button btn_login, btn_netSetting;

    //用于记录上一次按返回按钮的时间，用于连按两次返回退出
    private long mExitTime;

    /**
     * 从上一个activity返回后需要处理这些消息!!CAUTION!!返回的不要用0做标志位，默认就是0
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //情况只可能是下列情况的其中之一

        //服务器地址有问题
        if (requestCode == 890605 && resultCode == 19) {
            showNetSettingDialog();
        }
        //网络异常
        else if (requestCode == 890605 && resultCode == 3) {
            Config.getInstance().setIsAutologin(false);
        }
        //帐号不对头，应该把密码置空，并把自动登录项目关掉
        else if (requestCode == 890605 && resultCode == -1) {

            ed_password.setText("");
            Config.getInstance().setPassword("");
            Config.getInstance().setIsAutologin(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initView();//加载视图
        setListener();//绑定监听
    }

    @Override
    protected void onResume() {
        Test.LogD("Login_onResume");
        super.onResume();
        restoreState();//恢复数据
    }

    @Override
    protected void initView() {
        ed_account = (EditText) findViewById(R.id.account);
        ed_password = (EditText) findViewById(R.id.password);

        cb_isRempsw = (CheckBox) findViewById(R.id.isRempsw);
        cb_isAutologin = (CheckBox) findViewById(R.id.isAutologin);

        btn_login = (Button) findViewById(R.id.login);
        btn_netSetting = (Button) findViewById(R.id.netSetting);
    }

    @Override
    protected void setListener() {
        cb_isRempsw.setOnCheckedChangeListener(new checkChangedHandler());
        cb_isAutologin.setOnCheckedChangeListener(new checkChangedHandler());
        btn_login.setOnClickListener(new btnClickedHandler());
        btn_netSetting.setOnClickListener(new btnClickedHandler());
    }

    @Override
    protected void restoreState() {
        ed_account.setText(Config.getInstance().getAccount());
        cb_isRempsw.setChecked(Config.getInstance().getIsRempsw());

        //如果密码记忆选项开启的话，就恢复下密码
        if (cb_isRempsw.isChecked()) {
            String psw = Config.getInstance().getPassword();
            if (psw != null && !psw.equals(""))
                ed_password.setText(Encrypt.decryptPsw(psw));
        }

        cb_isAutologin.setChecked(Config.getInstance().getIsAutologin());

        //直接登录
        if (cb_isAutologin.isChecked()) {
            login();
        }
    }


    private void login() {
        String account = ed_account.getText().toString().trim();
        String password = ed_password.getText().toString().trim();

        if (account.equals("") || password.equals("")) {
            ToastUtil.showMsg("帐号或密码不能为空，请检查");
            return;
        }

        //保存帐号
        Config.getInstance().setAccount(account);
        //保存密码,不论如何，密码是一定要保存的。。。为了鉴权的需要
        Config.getInstance().setPassword(Encrypt.encryptPsw(password));


        //保存用户的设置
        Config.getInstance().setIsRempsw(cb_isRempsw.isChecked());
        Config.getInstance().setIsAutologin(cb_isAutologin.isChecked());

        Intent intent = new Intent(this, Loading.class);
        intent.putExtra("account", account);
        intent.putExtra("password", password);
        startActivityForResult(intent, 890605);
    }


    //点击menu按钮，弹出服务器地址配置窗口
    private void showNetSettingDialog() {
        final LayoutInflater factory = LayoutInflater.from(Login.this);
        final View view = factory.inflate(R.layout.url_setting, null);
        final EditText ed_url = (EditText) view.findViewById(R.id.url);//获得输入框对象
        ed_url.setText(Config.getInstance().getServiceUrl().replaceAll("/JHSoft.WCF/POSTServiceForAndroid.svc", ""));
        new AlertDialog.Builder(this)
                .setTitle("配置服务器地址")//提示框标题
                .setCancelable(false)//不允许通过后退关闭对话框
                .setView(view)
                .setPositiveButton("确定",//提示框的两个按钮
                        new DialogInterface.OnClickListener() {
                            private boolean isUrlInvalid(String url) {
                                Pattern pattern = Pattern.compile("^(http|www|ftp|)?(://)?(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*((:\\d+)?)(/(\\w+(-\\w+)*))*(\\.?(\\w)*)(\\?)?(((\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*(\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*)*(\\w*)*)$", Pattern.CASE_INSENSITIVE);
                                Matcher matcher = pattern.matcher(url);
                                if (!matcher.matches()) {
                                    Toast.makeText(Login.this, R.string.url_error_toast, Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = ed_url.getText().toString();
                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);//为false不能关闭对话框。为true则可关闭
                                    if (isUrlInvalid(url)) {
                                        Config.getInstance().setServiceUrl(url + "/JHSoft.WCF/POSTServiceForAndroid.svc");
                                        field.set(dialog, true);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                )
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).create().show();

    }

    //屏蔽返回按钮，按一次返回按钮提示“再按一次退出程序”
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtil.showMsg(getString(R.string.exit_hint));
                mExitTime = System.currentTimeMillis();

            } else {
                //该方法对2.2版本后有效
                finish();
                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                manager.killBackgroundProcesses(getPackageName());
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 为menu按键绑定事件
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 点击menu具体条目触发
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showNetSettingDialog();
                return true;
            case R.id.about_me:
                ToastUtil.showMsg("@Author:elvizlai");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class btnClickedHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.login:
                    login();
                    break;
                case R.id.netSetting:
                    showNetSettingDialog();
                    break;
                default:
                    break;
            }
        }
    }

    private class checkChangedHandler implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean chkState) {
            switch (compoundButton.getId()) {
                case R.id.isRempsw:
                    break;
                case R.id.isAutologin:
                    if (chkState) {
                        cb_isRempsw.setClickable(false);
                        cb_isRempsw.setChecked(true);
                    } else {
                        cb_isRempsw.setClickable(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
