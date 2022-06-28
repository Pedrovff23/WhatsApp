package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversas extends RecyclerView.Adapter<AdapterConversas.MyViewHolder> {

    private final List<Conversa> conversaList;
    private final android.content.Context context;

    public AdapterConversas(List<Conversa> conversaList, Context context) {
        this.conversaList = conversaList;
        this.context = context;
    }

    public List<Conversa> getConversaList (){
        return this.conversaList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_conversas, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversa conversa = conversaList.get(position);
        Log.i("Grupo", "" + conversa.getIsGroup());

        holder.ultimaConversa.setText(conversa.getUltimaMensagem());

            if(conversa.getIsGroup().equals("true")){

                Grupo grupo = conversa.getGrupo();
                holder.nome.setText(grupo.getNome());

                if (grupo.getFoto() != null && !grupo.getFoto().isEmpty()) {

                    Uri uri = Uri.parse(grupo.getFoto());
                    Glide.with(context).load(uri).into(holder.imagem);

                }else {

                    holder.imagem.setImageResource(R.drawable.padrao);
                }

            }else {

                Usuario usuario = conversa.getUsuarioExibicao();

                if(usuario != null){

                    String foto = usuario.getFoto();
                    String nome = usuario.getNome();
                    holder.nome.setText(nome);

                    if (foto != null && !foto.isEmpty()) {

                        Uri uri = Uri.parse(foto);
                        Glide.with(context).load(uri).into(holder.imagem);

                    } else {

                        holder.imagem.setImageResource(R.drawable.padrao);
                    }
                }

            }


    }

    @Override
    public int getItemCount() {
        return conversaList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView imagem;
        private final TextView nome;
        private final TextView ultimaConversa;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imagem = itemView.findViewById(R.id.circleImage_conversa);
            nome = itemView.findViewById(R.id.textView_nome_conversa);
            ultimaConversa = itemView.findViewById(R.id.textView_ultimaConversa_conversa);
        }
    }
}
