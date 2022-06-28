package com.example.whatsapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.databinding.ActivityConfiguracoesBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.OrientacaoImagem;
import com.example.whatsapp.helper.Permissao;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ConfiguracoesActivity extends AppCompatActivity {

    private final String[] permissoesNecessaria = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private final Context context = this;
    private ActivityConfiguracoesBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private StorageReference storageReference;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        storageReference = InstanciaFireBase
                .getStorageReference()
                .child("imagens")
                .child("perfil")
                .child(UsuarioFirebase.uidUsuario())
                .child("perfil.png");

        binding = ActivityConfiguracoesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        usuarioLogado = UsuarioFirebase.getDadosUsuario();

        //upload da imagem no firebase
        uploadImagemFirebase();

        //Configurando a toolbar
        setSupportActionBar(binding.toolbarConfiguracao.toolbarPrincipal);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Configurações");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recuperar dados do usuário
        recuperarDados();

        //Validar Permissões
        Permissao.validarPermissao(permissoesNecessaria, this, 1);

        binding.buttonCamera.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(intent);
                }
            }
        });

        binding.buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"QueryPermissionsNeeded", "IntentReset"})
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(intent);
                }
            }
        });

        binding.alterarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.editNome.getWindowToken(), 0);

                String campoNome = binding.editNome.getText().toString();

                if (!campoNome.isEmpty()) {
                    alterarNome(campoNome);
                }
            }
        });

    }

    private void recuperarDados() {

        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        Uri uri = user.getPhotoUrl();

        if (uri != null) {
            Glide.with(this)
                    .load(uri)
                    .into(binding.circleImageViewPerfil);
        } else {
            binding.circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }
        if (!Objects.requireNonNull(user.getDisplayName()).isEmpty()) {
            binding.editNome.setText(user.getDisplayName());
        }
    }


    public void alterarNome(String campoNome) {

        boolean retorno = UsuarioFirebase.atualizarNomeUsuario(campoNome);

        if (retorno) {
            usuarioLogado.setNome(campoNome);
            usuarioLogado.atualizarUsuario();

            Toast.makeText(ConfiguracoesActivity.this,
                    "Nome alterado", Toast.LENGTH_LONG).show();

        }

    }

    public void uploadImagemFirebase() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts
                        .StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bitmap imagem = null;

                            if (result.getData().getData() != null) {

                                Uri localImagemSelecionada = result.getData().getData();

                                try {
                                    imagem = OrientacaoImagem.carrega(localImagemSelecionada,
                                            ConfiguracoesActivity.this);

                                } catch (IOException e) {
                                    Log.i("Imagem", "" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }else {
                                Bundle bundle = result.getData().getExtras();
                                imagem = (Bitmap) bundle.get("data");
                            }

                            if (imagem != null) {
                                binding.circleImageViewPerfil.setImageBitmap(imagem);

                                //Recuperar dados da imagem para o firebase
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                imagem.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                byte[] dadosImagem = baos.toByteArray();

                                //salvar no firebase
                                StorageReference imageRef = storageReference;

                                UploadTask uploadTask = imageRef.putBytes(dadosImagem);

                                uploadTask.addOnFailureListener(new OnFailureListener() {

                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ConfiguracoesActivity.this,
                                                "Erro ao fazer upload de imagem",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener
                                        <UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Toast.makeText(ConfiguracoesActivity.this,
                                                "Sucesso ao fazer upload de imagem",
                                                Toast.LENGTH_LONG).show();

                                        imageRef.getDownloadUrl()
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<Uri>() {

                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task) {

                                                                Uri uri = task.getResult();
                                                                atualizarFotoUsuario(uri);
                                                            }
                                                        });
                                    }
                                });

                            }
                        }
                    }
                });
    }

    private void atualizarFotoUsuario(Uri uri) {
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(uri);

        if (retorno) {
            usuarioLogado.setFoto(uri.toString());
            usuarioLogado.atualizarUsuario();

            Toast.makeText(ConfiguracoesActivity.this
                    , "Sua foi alterada", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permisaoResultados : grantResults) {
            if (permisaoResultados == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setCancelable(true);
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
