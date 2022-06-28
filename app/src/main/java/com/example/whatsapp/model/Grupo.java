package com.example.whatsapp.model;

import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {
    private String id;
    private String nome;
    private String foto;
    private List<Usuario> membros;

    public Grupo(){
        DatabaseReference database = InstanciaFireBase.getDatabaseReference();
        DatabaseReference grupoRef = database.child("grupos");

        String idGrupoFireBase = grupoRef.push().getKey();
        setId(idGrupoFireBase);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }

    public void salvar(){

        DatabaseReference dataBase = InstanciaFireBase.getDatabaseReference();
        DatabaseReference grupoRef = dataBase.child("grupo");

        grupoRef.child(getId()).setValue(this);

        //Salvar conversas para membros do grupo
        for(Usuario membros : getMembros()){

            String idRemetente = membros.getCodigo();

            Conversa conversa = new Conversa();

            conversa.setIdRemetente(idRemetente);
            conversa.setIdDestinatario(getId());
            conversa.setUltimaMensagem("");
            conversa.setIsGroup("true");
            conversa.setGrupo(this);
            conversa.salvarConversaRemetente();
        }
    }
}
