package ru.prsolution.winstrike.ui.login;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.prsolution.winstrike.R;
import ru.prsolution.winstrike.WinstrikeApp;
import ru.prsolution.winstrike.common.YandexWebView;
import ru.prsolution.winstrike.common.utils.AuthUtils;
import ru.prsolution.winstrike.common.utils.TextFormat;
import ru.prsolution.winstrike.databinding.AcLoginBinding;
import ru.prsolution.winstrike.mvp.apimodels.AuthResponse;
import ru.prsolution.winstrike.mvp.apimodels.ConfirmSmsModel;
import ru.prsolution.winstrike.mvp.models.LoginViewModel;
import ru.prsolution.winstrike.mvp.models.MessageResponse;
import ru.prsolution.winstrike.mvp.presenters.SignInPresenter;
import ru.prsolution.winstrike.mvp.views.SignInView;
import ru.prsolution.winstrike.networking.Service;
import ru.prsolution.winstrike.ui.Screens;
import ru.prsolution.winstrike.ui.main.MainScreenActivity;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.commands.Back;
import ru.terrakok.cicerone.commands.Command;
import ru.terrakok.cicerone.commands.Forward;
import ru.terrakok.cicerone.commands.Replace;
import ru.terrakok.cicerone.commands.SystemMessage;
import timber.log.Timber;

import static ru.prsolution.winstrike.common.utils.TextFormat.formatPhone;
import static ru.prsolution.winstrike.common.utils.TextFormat.setTextFoot1Color;
import static ru.prsolution.winstrike.common.utils.TextFormat.setTextFoot2Color;
import static ru.prsolution.winstrike.common.utils.Utils.setBtnEnable;

/*
 * Created by oleg on 31.01.2018.
 */
// TODO: 13/05/2018 Reorder method call in this activity.
public class SignInActivity extends AppCompatActivity implements SignInView {
    private LoginViewModel loginViewModel;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.et_phone)
    EditText mPhoneView;
    @BindView(R.id.et_password)
    EditText mPasswordView;
    @BindView(R.id.v_button)
    View mSignInButton;

    @BindView(R.id.text_button_title)
    TextView mSignInButtonLabel;


    @BindView(R.id.text_help_link)
    TextView mHelpLinkView;

    @BindView(R.id.text_footer)
    TextView mFooterAccountView;

    @BindView(R.id.text_footer2)
    TextView mFooterSingUpView;


    @BindView(R.id.text_pol2)
    TextView conditionsButton;

    @BindView(R.id.text_pol4)
    TextView privacyButton;


//    @Inject
//    Router router;

//    @Inject
//    NavigatorHolder navigatorHolder;

    @Inject
    public Service mService;

    //    @InjectPresenter
    SignInPresenter mSignInPresenter;

    //    @ProvidePresenter
    public SignInPresenter createSignInPresenter() {
        return new SignInPresenter(mService, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        WinstrikeApp.getInstance().getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        renderView();
        init();

    }

    public void init() {
        TextFormat.formatText(mPhoneView, "(___) ___-__-__");

        setBtnEnable(mSignInButton, true);

        mSignInButton.setOnClickListener(
                it -> {
                    if (mPhoneView.getText().length() >= 14 && mPasswordView.getText().length() >=6) {
                        loginViewModel.setUsername(formatPhone(String.valueOf(mPhoneView.getText())));
                        loginViewModel.setPassword(String.valueOf(mPasswordView.getText()));
                        AuthUtils.INSTANCE.setPhone(loginViewModel.getUsername());
                        AuthUtils.INSTANCE.setPassword(loginViewModel.getPassword());

                        mSignInPresenter.signIn(loginViewModel);

                    } else if (TextUtils.isEmpty(mPhoneView.getText())){
                        toast("Введите номер телефона");
                    } else if (TextUtils.isEmpty(mPasswordView.getText())) {
                        toast("Пароль не должен быть пустым");
                    } else if (mPhoneView.getText().length() < 14) {
                        toast("Номер телефона не верный");
                    } else if (mPasswordView.getText().length() < 6) {
                        toast("Длина пароля должна не менее 6 символов");
                    }
                }
        );

//        checkFieldEnabled(mPhoneView, mPasswordView, mSignInButton);

        mHelpLinkView.setOnClickListener(
                it -> startActivity(new Intent(this, HelpActivity.class))
        );

        setFooter();
    }

    private void setFooter() {
        setTextFoot1Color(mFooterAccountView, "Еще нет аккаунта?", "#9b9b9b");
        setTextFoot2Color(mFooterSingUpView, " Зарегистрируйтесь", "#c9186c");
        mFooterSingUpView.setOnClickListener(
                it -> startActivity(new Intent(this, RegisterActivity.class))
        );

        String mystring = new String("Условиями");
        SpannableString content = new SpannableString(mystring);
        content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);
        conditionsButton.setText(content);


        conditionsButton.setOnClickListener(
                it -> {
                    Intent browserIntent = new Intent(this, YandexWebView.class);
                    String url = "file:///android_asset/rules.html";
                    browserIntent.putExtra("url", url);
                    startActivity(browserIntent);
                }
        );

        privacyButton.setOnClickListener(
                it -> {
                    Intent browserIntent = new Intent(this, YandexWebView.class);
                    String url = "file:///android_asset/politika.html";
                    browserIntent.putExtra("url", url);
                    startActivity(browserIntent);
                }
        );

        String textFooter = new String("Политикой конфиденциальности");
        SpannableString content4 = new SpannableString(textFooter);
        content4.setSpan(new UnderlineSpan(), 0, textFooter.length(), 0);
        privacyButton.setText(content4);

    }


    public void renderView() {
        //  setContentView(R.layout.ac_login);

        loginViewModel = new LoginViewModel();

        AcLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.ac_login);
        ButterKnife.bind(this);

        binding.setLoginModel(loginViewModel);

    }

    @Override
    public void showWait() {
//        showProgressDialog();
    }

    @Override
    public void removeWait() {
//        hideProgressDialog();
    }


    @Override
    public void onSendSmsSuccess(MessageResponse confirmModel) {
        Timber.tag("common").d("Sms send success: %s", confirmModel.getMessage());
//        toast("Код выслан повторно");
    }

    @Override
    public void onSmsSendFailure(String appErrorMessage) {
        Timber.tag("common").w("Sms send error: %s", appErrorMessage);
        if (appErrorMessage.contains("404"))
            toast("Ошибка отправки кода! Нет пользователя с таким номером");
        if (appErrorMessage.contains("409")) toast("Ошибка функции кодогенерации");
        if (appErrorMessage.contains("422")) toast("Не указан номер телефона");
    }

    /**
     * Success auth user. Save token
     *
     * @param authResponse - (token,isConfirmed)
     */
    @Override
    public void onAuthResponseSuccess(AuthResponse authResponse) {
        Boolean confirmed = authResponse.getUser().getConfirmed();

        updateUser(authResponse);

        if (confirmed) {
//                        router.replaceScreen(Screens.START_SCREEN);
            startActivity(new Intent(this, MainScreenActivity.class));
            Timber.d("Success signIn");
        } else {
            ConfirmSmsModel smsModel = new ConfirmSmsModel();
            smsModel.setUsername(authResponse.getUser().getPhone());
            mSignInPresenter.sendSms(smsModel);

            Intent intent = new Intent(this, UserConfirmActivity.class);
            intent.putExtra("phone", smsModel.getUsername());
            startActivity(intent);
        }

    }

    private void updateUser(AuthResponse authResponse) {
        AuthUtils.INSTANCE.setName(authResponse.getUser().getName());
        AuthUtils.INSTANCE.setToken(authResponse.getToken());
        AuthUtils.INSTANCE.setPhone(authResponse.getUser().getPhone());
        AuthUtils.INSTANCE.setConfirmed(authResponse.getUser().getConfirmed());
        AuthUtils.INSTANCE.setPublicid(authResponse.getUser().getPublicId());
    }

    @Override
    public void onAuthFailure(String appErrorMessage) {
        Timber.e("Error on auth: %s", appErrorMessage);
        if (appErrorMessage.contains("403")) toast("Неправильный пароль");
        if (appErrorMessage.contains("404")) {
            toast("Пользователь не найден");
        }
        if (appErrorMessage.contains("502")) toast("Ошибка сервера");
        if (appErrorMessage.contains("No Internet Connection!"))
            toast("Интернет подключение не доступно!");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSignInPresenter == null) {
            mSignInPresenter = createSignInPresenter();
        }
//        navigatorHolder.setNavigator(navigator);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Авторизация...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }



/*
    protected void checkFieldEnabled(EditText et_phone, EditText et_pass, View button) {
        Observable<TextViewTextChangeEvent> phoneObservable = RxTextView.textChangeEvents(et_phone);
        Observable<TextViewTextChangeEvent> passwordObservable = RxTextView.textChangeEvents(et_pass);
        Observable.combineLatest(phoneObservable, passwordObservable, (phoneSelected, passwordSelected) -> {
            boolean phoneCheck = phoneSelected.text().length() >= 14;
            boolean passwordCheck = passwordSelected.text().length() >= 4;
            return phoneCheck && passwordCheck;
        }).subscribe(aBoolean -> {
            if (aBoolean) {
                setBtnEnable(button, true);
            } else {
                setBtnEnable(button, false);
            }
        });
    }
*/

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Navigator navigator = new Navigator() {

        @Override
        public void applyCommands(Command[] commands) {
            for (Command command : commands) applyCommand(command);
        }

        private void applyCommand(Command command) {
            if (command instanceof Forward) {
                forward((Forward) command);
            } else if (command instanceof Replace) {
                replace((Replace) command);
            } else if (command instanceof Back) {
                back();
            } else if (command instanceof SystemMessage) {
//                Toast.makeText(StartActivity.this, ((SystemMessage) command).getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Cicerone", "Illegal command for this screen: " + command.getClass().getSimpleName());
            }
        }

        private void forward(Forward command) {
            switch (command.getScreenKey()) {
/*                case Screens.START_ACTIVITY_SCREEN:
                    startActivity(new Intent(StartActivity.this, StartActivity.class));
                    break;
                case Screens.MAIN_ACTIVITY_SCREEN:
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    break;
                case Screens.BOTTOM_NAVIGATION_ACTIVITY_SCREEN:
                    startActivity(new Intent(StartActivity.this, BottomNavigationActivity.class));
                    break;
                case Screens.PROFILE_SCREEN:
                    startActivity(new Intent(StartActivity.this, ProfileActivity.class));
                    break;
                default:
                    Log.e("Cicerone", "Unknown screen: " + command.getScreenKey());
                    break;*/
            }
        }

        private void replace(Replace command) {
            if (command.getScreenKey().equals(Screens.START_SCREEN)) {
                startActivity(new Intent(SignInActivity.this, MainScreenActivity.class));
            } else {
                Log.e("Cicerone", "Unknown screen: " + command.getScreenKey());
            }
        }

        private void back() {
            finish();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
        mSignInPresenter.onStop();
        mSignInPresenter = null;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("ru.prsolution.winstrike");
        finish();
    }



    public void onLogin(View view) {
        Timber.d("View on click fire");
    }

    /*    public class LoginHandler {

     *//*        public LoginHandler(LoginPresenter presenter) {
            this.presenter = presenter;
        }*//*

        public View.OnClickListener onLogin(final LoginViewModel viewModel) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    presenter.login(viewModel);
                    Timber.d("View on click fire");
                }
            };
        }

    }*/
}
