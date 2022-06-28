package com.example.whatsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.databinding.ValidarNumeroActivityBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class ValidarNumeroActivity extends AppCompatActivity {

    //"+16505554567"/123456 numero para teste
    //"+16505554562"/123452 numero para teste
    //+16505554563/123453 numero para teste

    private final FirebaseAuth auth = InstanciaFireBase.getAutenticacao();
    private final Context context = this;
    private ValidarNumeroActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ValidarNumeroActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(ValidarNumeroActivity.this,
                    TelaPrincipalActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        binding.buttonValidarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.buttonValidarNumero.getWindowToken(), 0);

                enviarValidacao();
            }
        });

    }

    private void enviarValidacao() {

        String numero = binding.campoNumeroTelefone.getText().toString();

        if (!numero.isEmpty()) {

            binding.buttonValidarNumero.setEnabled(false);
            binding.progressCircularNumero.setVisibility(View.VISIBLE);

            PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(numero)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential
                                                                    phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(ValidarNumeroActivity.this,
                                    "Número invalido", Toast.LENGTH_SHORT).show();

                            binding.progressCircularNumero.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId,
                                               @NonNull PhoneAuthProvider.ForceResendingToken
                                                       forceToken) {

                            abrirTelaPrincipal(verificationId);

                            super.onCodeSent(verificationId, forceToken);
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
        } else
            Toast.makeText(ValidarNumeroActivity.this,
                    "Digite seu numero", Toast.LENGTH_SHORT).show();
    }

    public void abrirTelaPrincipal(String verificationId) {

        if (!verificationId.isEmpty()) {
            Intent intent = new Intent(ValidarNumeroActivity.this,
                    ValidarSmsActivity.class);

            intent.putExtra("verificationId", verificationId);

            binding.progressCircularNumero.setVisibility(View.INVISIBLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onStart() {
        binding.buttonValidarNumero.setEnabled(true);
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual != null) {
            abrirTelaPrincipal("");
        }
        super.onStart();

    }
}