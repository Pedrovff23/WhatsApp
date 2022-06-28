package com.example.whatsapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.adapter.AdapterConversas;
import com.example.whatsapp.databinding.FragmentConversasBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;


public class ConversasFragment extends Fragment {
    private final List<Conversa> conversaList = new ArrayList<>();
    private final DatabaseReference dataBase = InstanciaFireBase.getDatabaseReference();
    private FragmentConversasBinding binding;
    private AdapterConversas adapterConversas;
    private Context context;
    private DatabaseReference conversasRefRemetente;
    private ChildEventListener eventListener;

    public ConversasFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConversasBinding.inflate(inflater, container, false);

        conversasRefRemetente = dataBase.child("conversas").child(UsuarioFirebase.uidUsuario());

        configurarRecyclerView();
        clickRecyclerView();

        return binding.getRoot();
    }

    public void configurarRecyclerView() {
        adapterConversas = new AdapterConversas(conversaList, context);
        binding.recyclerViewConversar.setHasFixedSize(true);
        binding.recyclerViewConversar.setAdapter(adapterConversas);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        recuperarConversasRemetente();
        super.onStart();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void recuperarConversasRemetente() {
        conversaList.clear();
        eventListener = conversasRefRemetente.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                conversaList.add(snapshot.getValue(Conversa.class));
                adapterConversas.notifyDataSetChanged();
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



    public void clickRecyclerView(){

        binding.recyclerViewConversar.addOnItemTouchListener(new RecyclerItemClickListener(
                context,
                binding.recyclerViewConversar,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        List<Conversa> listaConvesaAtualizada = adapterConversas.getConversaList();

                        Conversa conversa = listaConvesaAtualizada.get(position);

                        if(conversa.getIsGroup().equals("true")){

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("usuarioGrupo", conversa.getGrupo());
                            startActivity(intent);

                        }else {

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("usuario", conversa.getUsuarioExibicao());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void pesquisarConversas(String texto){
        List<Conversa> listaConversaBusca = new ArrayList<>();

        for (Conversa conversa : conversaList){
            if(conversa.getUsuarioExibicao() != null){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();

                if(nome.contains(texto) || ultimaMensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }else {
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMensagem = conversa.getUltimaMensagem().toLowerCase();

                if(nome.contains(texto) || ultimaMensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }

            }

        }
        adapterConversas = new AdapterConversas(listaConversaBusca,context);
        binding.recyclerViewConversar.setAdapter(adapterConversas);
        adapterConversas.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        conversasRefRemetente.removeEventListener(eventListener);
        super.onStop();
    }
}