package kz.incubator.sdcl.club1.Authentications;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import kz.incubator.sdcl.club1.MenuActivity;
import kz.incubator.sdcl.club1.R;

public class LoginPage extends Fragment {
    View view;
    Button btnLogin;
    EditText email;
    EditText password;
    FirebaseAuth auth;
    ProgressBar progressBar;
    TextView reset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(container.getContext()).inflate(R.layout.login_page,container,false);
        initWidgets();
        auth = FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emails = email.getText().toString();
                String passwords = password.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                if(TextUtils.isEmpty(emails) || TextUtils.isEmpty(passwords)){
                    Snackbar.make(btnLogin,"Please fill all info", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                else{
                    auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(getActivity(), MenuActivity.class));
                                    }
                                    else{
                                        String sub = task.getException() + "";
                                        String subbed = sub.substring(65,sub.length());
                                        Snackbar.make(btnLogin,subbed, Toast.LENGTH_SHORT).setActionTextColor(getResources().getColor(R.color.red)).show();
                                        progressBar.setVisibility(View.GONE);
                                        btnLogin.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                View second_view = getLayoutInflater().inflate(R.layout.forgot_password,null);
                alert.setView(second_view);
                final AlertDialog dialog = alert.create();
                dialog.show();
                final Button btn = second_view.findViewById(R.id.resetButton);
                final EditText email = second_view.findViewById(R.id.emailReset);
                final ProgressBar progressBarFor = second_view.findViewById(R.id.progressForReset);
                progressBarFor.setVisibility(View.GONE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBarFor.setVisibility(View.VISIBLE);
                        btn.setVisibility(View.GONE);
                        if(TextUtils.isEmpty(email.getText().toString())){
                            Snackbar.make(btn,"Fill It", Snackbar.LENGTH_SHORT).show();
                            progressBarFor.setVisibility(View.GONE);
                            btn.setVisibility(View.VISIBLE);
                        }
                        else{
                            auth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(btn, "We have sent you instructions to reset your password!", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    } else {
                                        Snackbar.make(btn, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        progressBarFor.setVisibility(View.GONE);
                                        btn.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        return view;
    }

    public void initWidgets() {
        btnLogin = view.findViewById(R.id.btnLogin);
        email = view.findViewById(R.id.emailToLogin);
        password = view.findViewById(R.id.passwordToLogin);
        progressBar = view.findViewById(R.id.progressBarForLogin);
        reset = view.findViewById(R.id.forgotPassword);
    }
}
