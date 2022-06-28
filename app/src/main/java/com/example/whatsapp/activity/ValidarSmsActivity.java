package com.example.whatsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.databinding.ValidarSmsActivityBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ValidarSmsActivity extends AppCompatActivity {

    private final DatabaseReference reference = InstanciaFireBase.getDatabaseReference();
    private ValidarSmsActivityBinding binding;
    private DatabaseReference validarUsuario;
    private ValueEventListener listener;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ValidarSmsActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        context = getApplicationContext();

        binding.buttonValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.buttonValidar.getWindowToken(), 0);

                validarSms();
            }
        });
    }

    private void validarSms() {

        String sms = binding.campoNumeroSms.getText().toString();

        if (!sms.isEmpty()) {
            binding.buttonValidar.setEnabled(false);
            binding.progressCircularSms.setVisibility(View.VISIBLE);

            String verificationId = getIntent().getStringExtra("verificationId");

            Usuario usuario = new Usuario();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, sms);

            logar(credential, usuario);

        } else {
            Toast.makeText(ValidarSmsActivity.this,
                    "Digite o codigo do sms", Toast.LENGTH_SHORT).show();
        }
    }

    private void logar(PhoneAuthCredential credential, Usuario usuario) {

        InstanciaFireBase.getAutenticacao().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            pegarUsusario(usuario);

                        } else {
                            binding.buttonValidar.setEnabled(true);
                            Toast.makeText(ValidarSmsActivity.this,
                                    "Codigo invalido", Toast.LENGTH_SHORT).show();
                            binding.progressCircularSms.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void pegarUsusario(Usuario usuario) {

        FirebaseAuth auth = InstanciaFireBase.getAutenticacao();
        FirebaseUser user = auth.getCurrentUser();

        validarUsuario = reference.child("usuarios").child(user.getUid());

        listener = validarUsuario.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean usuarioExist = snapshot.exists();

                if (!usuarioExist) {
                    binding.progressCircularSms.setVisibility(View.GONE);
                    binding.campoNumeroSms.setVisibility(View.INVISIBLE);
                    binding.campoNomeUsuario.setVisibility(View.VISIBLE);

                    binding.buttonValidar.setVisibility(View.INVISIBLE);
                    binding.buttonValidarNome.setVisibility(View.VISIBLE);

                    binding.buttonValidarNome.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    String nomeUsuario = binding
                                            .campoNomeUsuario
                                            .getText()
                                            .toString();

                                    if (!nomeUsuario.isEmpty()) {
                                        try {
                                            usuario.setNome(nomeUsuario);
                                            usuario.setTelefone(user.getPhoneNumber());
                                            usuario.setCodigo(UsuarioFirebase.uidUsuario());
                                            usuario.salvarUsuario();
                                            UsuarioFirebase.atualizarNomeUsuario(nomeUsuario);
                                            abrirTelaPrincipal();

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    } else {
                                        Toast.makeText(ValidarSmsActivity.this,
                                                "Digite seu nome",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
                } else {
                    abrirTelaPrincipal();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirTelaPrincipal() {
        Intent intent = new Intent(ValidarSmsActivity.this,
                TelaPrincipalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (listener != null) {
            validarUsuario.removeEventListener(listener);
        }

        super.onDestroy();
    }
}