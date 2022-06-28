package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterContatos extends RecyclerView.Adapter<AdapterContatos.MyViewHolder> {

    private final List<Usuario> usuarioList;
    private final Context context;

    public AdapterContatos(List<Usuario> usuarioList, Context context) {
        this.usuarioList = usuarioList;
        this.context = context;
    }

    public List<Usuario> getContatos(){
        return this.usuarioList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_contatos, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String nome = usuarioList.get(position).getNome();
        holder.textViewNome.setText(nome);

        String numero = usuarioList.get(position).getTelefone();
        boolean cabecalho = numero.isEmpty();
        holder.textViewNumero.setText(numero);



        if (usuarioList.get(position).getFoto() != null &&
                !usuarioList.get(position).getFoto().isEmpty()) {
            Uri uri = Uri.parse(usuarioList.get(position).getFoto());
            Glide.with(context).load(uri).into(holder.circleImageContatos);
        } else {
            if(cabecalho){
                holder.circleImageContatos.setImageResource(R.drawable.icone_grupo);
                holder.textViewNumero.setVisibility(View.GONE);
            }else {
                holder.textViewNumero.setVisibility(View.VISIBLE);
                holder.circleImageContatos.setImageResource(R.drawable.padrao);
            }
        }

    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewNome;
        private final TextView textViewNumero;
        private final CircleImageView circleImageContatos;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNome = itemView.findViewById(R.id.textView_nome);
            textViewNumero = itemView.findViewById(R.id.textView_numero);
            circleImageContatos = itemView.findViewById(R.id.circleImage_contatos);
        }
    }
}
