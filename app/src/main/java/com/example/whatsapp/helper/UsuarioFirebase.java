package com.example.whatsapp.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UsuarioFirebase {

    public static String uidUsuario(){
        FirebaseAuth auth = InstanciaFireBase.getAutenticacao();
        return auth.getUid();
    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth auth = InstanciaFireBase.getAutenticacao();
        return auth.getCurrentUser();
    }

    public static boolean atualizarFotoUsuario(Uri uri) {

        try {
            FirebaseUser user = getUsuarioAtual();

            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                    .Builder()
                    .setPhotoUri(uri)
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.d("Perfil", "Erro ao atualizar foto de perfil");
                            }
                        }
                    });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean atualizarNomeUsuario(String nome) {

        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.d("Perfil", "Erro ao atualizar nome de perfil");
                            }
                        }
                    });
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Usuario getDadosUsuario(){
        FirebaseUser usuario = getUsuarioAtual();

        Usuario dadosUsuario = new Usuario();

        dadosUsuario.setNome(usuario.getDisplayName());
        dadosUsuario.setTelefone(usuario.getPhoneNumber());
        dadosUsuario.setCodigo(uidUsuario());
        if(usuario.getPhotoUrl() == null){
            dadosUsuario.setFoto("");
        }else {
            dadosUsuario.setFoto(usuario.getPhotoUrl().toString());
        }

        return dadosUsuario;
    }
}
