package com.example.whatsapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static void validarPermissao(String[] permissoes, Activity activity, int requestCode){

        if(Build.VERSION.SDK_INT >= 23){

            List<String> listaPermissoes = new ArrayList<>();

            for (String permissao: permissoes){

              boolean temPermissao = ContextCompat.checkSelfPermission(activity,permissao)
                      == PackageManager.PERMISSION_GRANTED;

              if(!temPermissao){
                  listaPermissoes.add(permissao);
              }
            }

            String[]novasPermissoes = new String[listaPermissoes.size()];

            listaPermissoes.toArray(novasPermissoes);

            if(!listaPermissoes.isEmpty()){
                ActivityCompat.requestPermissions(activity,novasPermissoes,requestCode);
            }
        }
    }
}
