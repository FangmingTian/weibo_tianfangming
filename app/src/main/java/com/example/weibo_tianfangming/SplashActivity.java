package com.example.weibo_tianfangming;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_PRIVACY_AGREED = "privacyAgreed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean privacyAgreed = preferences.getBoolean(KEY_PRIVACY_AGREED, false);

        if (!privacyAgreed) {
            showPrivacyDialog();
        } else {
            navigateToMainActivity();
        }

        //showPrivacyDialog();
    }

    private void showPrivacyDialog() {
        setContentView(R.layout.activity_splash);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_privacy, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogLayout);

        AlertDialog dialog = builder.create();

        Button agreeButton = dialogLayout.findViewById(R.id.agreeButton);
        Button disagreeButton = dialogLayout.findViewById(R.id.disagreeButton);
        TextView privacyText = dialogLayout.findViewById(R.id.privacyText);

        String privacyMessage = "欢迎使用 iH微博! 我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》。";
        SpannableString spannableString = new SpannableString(privacyMessage);

        ClickableSpan userAgreementClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(SplashActivity.this, "查看用户协议", Toast.LENGTH_SHORT).show();
            }
        };

        ClickableSpan privacyPolicyClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(SplashActivity.this, "查看隐私协议", Toast.LENGTH_SHORT).show();
            }
        };

        spannableString.setSpan(userAgreementClick, privacyMessage.indexOf("《用户协议》"), privacyMessage.indexOf("《用户协议》") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyPolicyClick, privacyMessage.indexOf("《隐私政策》"), privacyMessage.indexOf("《隐私政策》") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), privacyMessage.indexOf("《用户协议》"), privacyMessage.indexOf("《用户协议》") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), privacyMessage.indexOf("《隐私政策》"), privacyMessage.indexOf("《隐私政策》") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        privacyText.setText(spannableString);
        privacyText.setMovementMethod(LinkMovementMethod.getInstance());

        agreeButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_PRIVACY_AGREED, true);
            editor.apply();
            dialog.dismiss();
            navigateToMainActivity();
        });

        disagreeButton.setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();  // 退出应用
        });

        dialog.show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
