package com.example.whatsapp.model;

import com.example.whatsapp.firebase.InstanciaFireBase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String nome;
    private String foto;
    private String telefone;
    private String codigo;


    public Usuario() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String numeroTelefone) {
        this.telefone = numeroTelefone;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public void salvarUsuario() {
        String uid = InstanciaFireBase.getAutenticacao().getCurrentUser().getUid();
        DatabaseReference reference = InstanciaFireBase.getDatabaseReference();
        reference.child("usuarios").child(uid).setValue(this);
    }

    @Exclude
    public void atualizarUsuario() {
        String uid = InstanciaFireBase.getAutenticacao().getCurrentUser().getUid();
        DatabaseReference databaseReference = InstanciaFireBase.getDatabaseReference();
        DatabaseReference usuarioRef = databaseReference.child("usuarios").child(uid);
        usuarioRef.updateChildren(converterParaMap());
    }

    @Exclude
    public Map<String, Object> converterParaMap() {
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("nome", getNome());
        usuarioMap.put("telefone", getTelefone());
        usuarioMap.put("foto", getFoto());
        usuarioMap.put("codigo", getCodigo());
        return usuarioMap;
    }
}
