package com.example.whatsapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.activity.GrupoActivity;
import com.example.whatsapp.adapter.AdapterContatos;
import com.example.whatsapp.adapter.AdapterConversas;
import com.example.whatsapp.databinding.FragmentContatosBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContatosFragment extends Fragment {

    private final List<Usuario> usuarioList = new ArrayList<>();
    private FragmentContatosBinding binding;
    private DatabaseReference reference;
    private ValueEventListener eventListener;
    private Context context;
    private AdapterContatos adapterContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContatosBinding
                .inflate(inflater, container, false);

        //configuraçoes inicias
        reference = InstanciaFireBase
                .getDatabaseReference()
                .child("usuarios");

        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configuração do RecyclerView
        configuracaoRecyclerView();
        configurarItensRecyclerView();

        //********
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        listarUsuarioFirebase();
        super.onStart();
    }

    public void configuracaoRecyclerView() {
        adapterContatos = new AdapterContatos(usuarioList, context);
        binding.recyclerViewContatos.setAdapter(adapterContatos);
        binding.recyclerViewContatos.setHasFixedSize(true);
    }

    public void configurarItensRecyclerView() {

        binding.recyclerViewContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(context,
                        binding.recyclerViewContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                List<Usuario> listAtualizada = adapterContatos.getContatos();

                                Usuario usuarioSelecionado = listAtualizada.get(position);
                                boolean cabecalho = usuarioSelecionado.getTelefone().isEmpty();

                                if(cabecalho){

                                    Intent intent = new Intent(context, GrupoActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }else {

                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("usuario", usuarioSelecionado);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }


                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        })
        );
    }


    public void listarUsuarioFirebase() {

        usuarioList.clear();

        //adicionarGrupo
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setTelefone("");

        usuarioList.add(itemGrupo);
        eventListener = reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dados : snapshot.getChildren()) {

                    Usuario usuario = dados.getValue(Usuario.class);
                    usuario.setCodigo(dados.getKey());
                    String uid = usuarioAtual.getUid();
                    if (!uid.equals(usuario.getCodigo()))
                        usuarioList.add(usuario);
                }
                adapterContatos.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void pesquisarContatos(String texto){
        List<Usuario> listaContatosBusca = new ArrayList<>();

        for (Usuario usuario : usuarioList) {
            String nome = usuario.getNome().toLowerCase();
            if(nome.contains(texto)){
                listaContatosBusca.add(usuario);
            }
        }
        adapterContatos = new AdapterContatos(listaContatosBusca,context);
        binding.recyclerViewContatos.setAdapter(adapterContatos);
        adapterContatos.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        reference.removeEventListener(eventListener);
        binding = null;
    }
}
