package com.example.whatsapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.whatsapp.adapter.AdapterContatos;
import com.example.whatsapp.adapter.AdapterContatosSelecionados;
import com.example.whatsapp.databinding.ActivityGrupoBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrupoActivity extends AppCompatActivity {
    private final List<Usuario> usuarioList = new ArrayList<>();
    private final List<Usuario> usuarioListSelecionados = new ArrayList<>();
    private final DatabaseReference dataBase = InstanciaFireBase.getDatabaseReference();
    private ActivityGrupoBinding binding;
    private AdapterContatos adapterContatos;
    private AdapterContatosSelecionados adapterContatosSelecionados;
    private DatabaseReference usuariosFireBase;
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        usuariosFireBase = dataBase.child("usuarios");

        //Configurar a toolbar
        setSupportActionBar(binding.toolbarGrupo.toolbarPrincipal);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Novo grupo");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        itemClickAdicionar();
        itemClickRemover();
        congRecyclerMembros();
        congRecyclerMembrosSelecionados();

        binding.botaoCriarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usuarioListSelecionados.size() >= 1){
                    Intent intent = new Intent(getApplicationContext(),CadastroGrupoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("usuariosSelecionados",
                            (Serializable) usuarioListSelecionados);
                    startActivity(intent);
                }else {
                    Toast.makeText(GrupoActivity.this,
                            "Selecione pelo menos 1 pessoa",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        pegarUsuarioFirebase();
        super.onStart();
    }

    private void congRecyclerMembros() {
        adapterContatos = new AdapterContatos(usuarioList, getApplicationContext());
        binding.contentGrupo.recyclerViewMembros.setHasFixedSize(true);
        binding.contentGrupo.recyclerViewMembros.setAdapter(adapterContatos);
    }

    private void congRecyclerMembrosSelecionados() {

        binding.contentGrupo.recyclerViewMembrosSelecionados.
                setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL, false));

        adapterContatosSelecionados = new AdapterContatosSelecionados(usuarioListSelecionados,
                getApplicationContext());

        binding.contentGrupo.recyclerViewMembrosSelecionados.setHasFixedSize(true);
        binding.contentGrupo.recyclerViewMembrosSelecionados.setAdapter(adapterContatosSelecionados);
    }

    private void pegarUsuarioFirebase() {
        eventListener = usuariosFireBase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    boolean igual = false;

                    for (Usuario usuarioIgual : usuarioListSelecionados) {
                        assert usuario != null;
                        if (Objects.equals(usuarioIgual.getTelefone(), usuario.getTelefone())) {
                            igual = true;
                            break;
                        }
                    }
                    if (!igual) {
                        assert usuario != null;
                        if(!usuario.getTelefone().equals(UsuarioFirebase
                                .getDadosUsuario().getTelefone())){
                            usuarioList.add(usuario);
                        }
                    }
                }
                adapterContatos.notifyDataSetChanged();
                atualizarToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void itemClickAdicionar() {
        binding.contentGrupo.recyclerViewMembros
                .addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                        binding.contentGrupo.recyclerViewMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuario = usuarioList.get(position);

                                boolean igual = false;

                                for (Usuario usuarioIgual : usuarioListSelecionados) {
                                    if (usuarioIgual.equals(usuario)) {
                                        igual = true;
                                        break;
                                    }
                                }
                                if (!igual) {
                                    usuarioListSelecionados.add(usuario);
                                    adapterContatosSelecionados.notifyDataSetChanged();
                                    usuarioList.remove(position);
                                    adapterContatos.notifyDataSetChanged();
                                    atualizarToolbar();
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }));
    }

    public void itemClickRemover() {
        binding.contentGrupo.recyclerViewMembrosSelecionados
                .addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                        binding.contentGrupo.recyclerViewMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuario = usuarioListSelecionados.get(position);
                                usuarioList.add(usuario);
                                adapterContatos.notifyDataSetChanged();
                                usuarioListSelecionados.remove(position);
                                adapterContatosSelecionados.notifyDataSetChanged();
                                atualizarToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }));

    }

    private void atualizarToolbar(){

        int selecionado = usuarioListSelecionados.size();
        int total = usuarioList.size() + selecionado;
        Objects.requireNonNull(getSupportActionBar())
                .setSubtitle(selecionado + " de " + total + " Selecionados");
    }

    @Override
    protected void onStop() {
        usuariosFireBase.removeEventListener(eventListener);
        super.onStop();
    }
}