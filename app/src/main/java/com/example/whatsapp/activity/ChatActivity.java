package com.example.whatsapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.databinding.ActivityChatBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.fragment.ConversasFragment;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final List<Mensagem> mensagemList = new ArrayList<>();
    private ActivityChatBinding binding;

    //identificador usuario remetente e destinatario
    private Usuario usuarioRemetente;
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private MensagensAdapter adapter;
    private DatabaseReference mensagemRef;
    private ChildEventListener eventListener;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Usuario usuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Recuperar dados remetente
        idUsuarioRemetente = UsuarioFirebase.uidUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuario();


        //toolbar
        setSupportActionBar(binding.toolbarChat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        recuperarDadosToolbar();

        DatabaseReference database = InstanciaFireBase.getDatabaseReference();
        mensagemRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        binding.contentChat.enviarFoto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    resultLauncher.launch(intent);
                }
            }
        });

        uploadImage();

        configurarRecyclerView();

    }

    private void recuperarDadosToolbar() {

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            if(bundle.containsKey("usuarioGrupo")){

                grupo = (Grupo) bundle.getSerializable("usuarioGrupo");
                idUsuarioDestinatario = grupo.getId();
                binding.nomeToolbar.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if (foto != null && !foto.isEmpty()) {
                    Uri uri = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(uri).into(binding.imagemToolbar);
                } else {
                    binding.imagemToolbar.setImageResource(R.drawable.padrao);
                }

            }else {

                usuarioDestinatario = (Usuario) bundle.getSerializable("usuario");
                String nome = usuarioDestinatario.getNome();
                binding.nomeToolbar.setText(nome);

                String foto = usuarioDestinatario.getFoto();
                if (foto != null && !foto.isEmpty()) {
                    Uri uri = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(uri).into(binding.imagemToolbar);
                } else {
                    binding.imagemToolbar.setImageResource(R.drawable.padrao);
                }
                //Recuperar dados destinatario
                idUsuarioDestinatario = usuarioDestinatario.getCodigo();
            }
        }
    }


    public void enviarMensagem(View view) {

        String textoMensagem = binding.contentChat.escreverMensagem.getText().toString();

        if (!textoMensagem.isEmpty()) {

            if (usuarioDestinatario != null) {

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);


                //Salvar mensagem
                salvarMensagemRemetente(mensagem,idUsuarioRemetente);
                salvarMensagemDestinatario(mensagem, idUsuarioDestinatario);

                //Salvar uma conversa remetente
                salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,
                        usuarioDestinatario,mensagem,false);

                //Salvar uma conversa destinatario
                salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioRemetente,
                        mensagem,false);

            }else {

                for(Usuario membro: grupo.getMembros()){

                    String idRemetenteGrupo = membro.getCodigo();
                    String idUsuarioLogadoGrupo = UsuarioFirebase.uidUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setNome(usuarioRemetente.getNome());
                    mensagem.setMensagem(textoMensagem);

                    //salvar mensagem
                    salvarMensagemGrupo(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //Salvar uma conversa
                    salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,
                            mensagem,true);
                }
            }


        } else {
            Toast.makeText(this,
                    "Digite a mensagem para enviar",
                    Toast.LENGTH_SHORT).show();
        }
        //limpar Texto
        binding.contentChat.escreverMensagem.setText("");
    }

    private void salvarConversa(String idRemetente, String idDestinatario,
                                Usuario exibicao, Mensagem mensagem, boolean isGrup) {

        Conversa conversa = new Conversa();

        conversa.setIdRemetente(idRemetente);
        conversa.setIdDestinatario(idDestinatario);
        conversa.setUltimaMensagem(mensagem.getMensagem());

        if(isGrup){

            conversa.setIsGroup("true");
            conversa.setGrupo(grupo);

        }else {
            conversa.setIsGroup("false");
            conversa.setUsuarioExibicao(exibicao);

        }
        conversa.salvarConversaRemetente();
    }


    private void salvarMensagemGrupo(String idRemetente, String idDestinatario, Mensagem mensagem) {

        DatabaseReference database = InstanciaFireBase.getDatabaseReference();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(mensagem);
    }

    private void salvarMensagemRemetente(Mensagem mensagem, String idRemetente) {

        DatabaseReference database = InstanciaFireBase.getDatabaseReference();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idUsuarioDestinatario)
                .child(idRemetente)
                .push()
                .setValue(mensagem);
    }

    private void salvarMensagemDestinatario(Mensagem mensagem,String idDestinatario) {

        DatabaseReference database = InstanciaFireBase.getDatabaseReference();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idUsuarioRemetente)
                .child(idDestinatario)
                .push()
                .setValue(mensagem);
    }

    public void configurarRecyclerView() {

        adapter = new MensagensAdapter(mensagemList, getApplicationContext());
        binding.contentChat.recyclerViewChat.setAdapter(adapter);
        binding.contentChat.recyclerViewChat.setHasFixedSize(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagem();
    }

    private void recuperarMensagem() {

        mensagemList.clear();

        eventListener = mensagemRef.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagemList.add(mensagem);
                binding.contentChat.recyclerViewChat.scrollToPosition(mensagemList.size() - 1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void uploadImage() {
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @Override
                    public void onActivityResult(ActivityResult result) {

                        assert result.getData() != null;
                        if (result.getData().getExtras() != null) {
                            try {

                                Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                                binding.contentChat.escreverMensagem.setEnabled(false);
                                binding.contentChat.escreverMensagem
                                        .setHint("Enviando sua mensagem aguarde....");
                                salvarImagemFirebase(bitmap);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }

    public void salvarImagemFirebase(Bitmap bitmap){

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);

        byte[] bytes = outputStream.toByteArray();

        //criar nome da imagem
        String nomeImagem = UUID.randomUUID().toString() + ".jpeg";

        StorageReference storageReference = InstanciaFireBase.getStorageReference()
                .child("imagens").child("mensagens")
                .child(idUsuarioRemetente)
                .child(nomeImagem);

        UploadTask uploadTask = storageReference.putBytes(bytes);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ERRO", "Erro ao fazer upload");
                Toast.makeText(ChatActivity.this,
                        "Erro ao fazer upload",Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uriMensagem = task.getResult();

                        if(usuarioDestinatario != null){//mensagem normal

                            Mensagem mensagem = new Mensagem();
                            mensagem.setIdUsuario(idUsuarioRemetente);
                            mensagem.setMensagem("imagem.jpeg");
                            mensagem.setImagem(uriMensagem.toString());

                            salvarMensagemRemetente(mensagem, idUsuarioRemetente);
                            salvarMensagemDestinatario(mensagem, idUsuarioDestinatario);

                        }else {//mensagem em grupo

                            for(Usuario membro: grupo.getMembros()){

                                String idRemetenteGrupo = membro.getCodigo();
                                String idUsuarioLogadoGrupo = UsuarioFirebase.uidUsuario();

                                Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                mensagem.setNome(usuarioRemetente.getNome());
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setImagem(uriMensagem.toString());

                                //salvar mensagem
                                salvarMensagemGrupo(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                //Salvar uma conversa
                                salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,
                                        mensagem,true);
                            }

                        }
                    }
                });

                Toast.makeText(ChatActivity.this,
                                "Sucesso ao fazer upload",Toast.LENGTH_SHORT)
                        .show();

                binding.contentChat.escreverMensagem
                        .setHint("Digite sua mensagem");
                binding.contentChat.escreverMensagem.setEnabled(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagemRef.removeEventListener(eventListener);

    }
}