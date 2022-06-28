package com.example.whatsapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.adapter.AdapterContatosSelecionados;
import com.example.whatsapp.databinding.ActivityCadastroGrupoBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.OrientacaoImagem;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CadastroGrupoActivity extends AppCompatActivity {
    private final List<Usuario> membros = new ArrayList<>();
    private ActivityCadastroGrupoBinding binding;
    private ActivityResultLauncher<Intent> resultLauncher;
    private StorageReference reference;
    private Grupo grupo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarConfiguracaoGrupo.toolbarPrincipal);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Novo Grupo");
        getSupportActionBar().setSubtitle("Defina o nome");

        grupo = new Grupo();

        reference = InstanciaFireBase.getStorageReference();

        if (getIntent().getExtras() != null) {

            membros.addAll((List<Usuario>) getIntent()
                    .getSerializableExtra("usuariosSelecionados"));
            binding.contentCadastroGrupo
                    .numeroParticipantes.setText("Participantes: " + membros.size());
        }

        configurarRecyclerView();

        binding.contentCadastroGrupo.imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    resultLauncher.launch(intent);
                }

            }
        });

        recuperarImagem();

        binding.botaoAdicionarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nomeGrupo = binding.contentCadastroGrupo.editNomeGrupo.getText().toString();

                membros.add(UsuarioFirebase.getDadosUsuario());
                grupo.setMembros(membros);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("usuarioGrupo", grupo);
                startActivity(intent);
            }
        });

    }

    private void configurarRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        AdapterContatosSelecionados contatosSelecionados = new AdapterContatosSelecionados(membros,
                getApplicationContext());
        binding.contentCadastroGrupo.recyclerViewCadastroGrupo.setLayoutManager(layoutManager);
        binding.contentCadastroGrupo.recyclerViewCadastroGrupo.setAdapter(contatosSelecionados);
        binding.contentCadastroGrupo.recyclerViewCadastroGrupo.setHasFixedSize(true);
    }

    private void recuperarImagem() {
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData().getData() != null) {
                    Bitmap imagem = null;
                    Uri uri = result.getData().getData();
                    try {
                        imagem = OrientacaoImagem.carrega(uri, getApplicationContext());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (imagem != null) {
                        binding.contentCadastroGrupo.imageGrupo.setImageBitmap(imagem);
                        binding.botaoAdicionarGrupo.setVisibility(View.INVISIBLE);
                        binding.progressCircularGrupo.setVisibility(View.VISIBLE);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] bytes = baos.toByteArray();
                        String nomeFoto = grupo.getId() + ".jpeg";

                        StorageReference storage = reference
                                .child("imagens")
                                .child("grupos")
                                .child(nomeFoto);
                        UploadTask uploadTask = storage.putBytes(bytes);

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.botaoAdicionarGrupo.setVisibility(View.VISIBLE);
                                binding.progressCircularGrupo.setVisibility(View.INVISIBLE);

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        binding.botaoAdicionarGrupo.setVisibility(View.VISIBLE);
                                        binding.progressCircularGrupo.setVisibility(View.INVISIBLE);
                                        Uri uriFoto = task.getResult();
                                        grupo.setFoto(uriFoto.toString());
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }
}