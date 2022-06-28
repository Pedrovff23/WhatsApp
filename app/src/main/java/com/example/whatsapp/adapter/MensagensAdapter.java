package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Mensagem;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private final List<Mensagem> mensagemList;
    private final Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> mensagemList, Context context){
        this.mensagemList = mensagemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item;

        if (viewType == TIPO_REMETENTE){
             item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_remetente,parent,false);
        }else{
            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_mensagem_destinatario,parent,false);
        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Mensagem mensagem = mensagemList.get(position);

        String texto = mensagem.getMensagem();
        String foto = mensagem.getImagem();

        if(foto != null && !foto.isEmpty()){
            holder.texto.setVisibility(View.GONE);
            Uri uri = Uri.parse(foto);
            holder.imagem.setVisibility(View.VISIBLE);
            Glide.with(context).load(uri).into(holder.imagem);

            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.textoNome.setText(nome);
            }else {
                holder.textoNome.setVisibility(View.GONE);
            }
        }else {
            holder.imagem.setVisibility(View.GONE);
            holder.texto.setVisibility(View.VISIBLE);
            holder.texto.setText(texto);
            String nome = mensagem.getNome();
            if(!nome.isEmpty()){
                holder.textoNome.setText(nome);
            }else {
                holder.textoNome.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mensagemList.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = mensagemList.get(position);
        String idUsuario = UsuarioFirebase.uidUsuario();

        if(idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_REMETENTE;
        }
        return TIPO_DESTINATARIO;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imagem;
        private final TextView texto;
        private final TextView textoNome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNome = itemView.findViewById(R.id.textNomeExibicao);
            texto = itemView.findViewById(R.id.textMensagemTexto);
            imagem = itemView.findViewById(R.id.imagemMensagemFoto);

        }
    }

}

