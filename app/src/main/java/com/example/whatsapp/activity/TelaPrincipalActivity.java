package com.example.whatsapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.whatsapp.R;
import com.example.whatsapp.databinding.TelaPrincipalActivityBinding;
import com.example.whatsapp.firebase.InstanciaFireBase;
import com.example.whatsapp.fragment.ContatosFragment;
import com.example.whatsapp.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Objects;

public class TelaPrincipalActivity extends AppCompatActivity {
    private TelaPrincipalActivityBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_principal_activity);
        auth = InstanciaFireBase.getAutenticacao();

        binding = TelaPrincipalActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //Configurando a toolbar
        setSupportActionBar(binding.toolbarPrincipal.toolbarPrincipal);
        Objects.requireNonNull(getSupportActionBar()).setTitle("WhatsApp");

        //Condigurando abas
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Conversa", ConversasFragment.class)
                        .add("Contatos", ContatosFragment.class)
                        .create()
        );

        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(adapter);

        SmartTabLayout smartTabLayout = binding.viewpagertab;
        smartTabLayout.setViewPager(viewPager);

        binding.toolbarPrincipal.searchView.setOnQueryTextListener(
                new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(@NonNull String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(@NonNull String newText) {
                        //Verificar se esta pesquisando conversas ou contatos
                        switch (viewPager.getCurrentItem()) {
                            case 0:
                                ConversasFragment conversasFragment = (ConversasFragment) adapter
                                        .getPage(0);
                                if (!newText.isEmpty()) {
                                    conversasFragment.pesquisarConversas(newText.toLowerCase());
                                } else {
                                    conversasFragment.configurarRecyclerView();
                                }
                                break;
                            case 1:
                                ContatosFragment contatosFragment = (ContatosFragment) adapter
                                        .getPage(1);
                                if (!newText.isEmpty()) {
                                    contatosFragment.pesquisarContatos(newText.toLowerCase());
                                } else {
                                    contatosFragment.configuracaoRecyclerView();
                                }
                                break;
                        }
                        return true;
                    }
                });

        binding.toolbarPrincipal.searchView.setOnSearchViewListener(
                new MaterialSearchView.SearchViewListener() {
                    @Override
                    public void onSearchViewShown() {

                    }

                    @Override
                    public void onSearchViewClosed() {
                        ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                        fragment.configurarRecyclerView();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.item_menu, menu);

        MenuItem item = menu.findItem(R.id.menuPesquisa);

        binding.toolbarPrincipal.searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuPesquisa:
                break;

            case R.id.menuConfiguracoes:
                abrirConfiguracoes();
                break;

            case R.id.menuSair:
                deslogarUsuario();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario() {
        try {
            auth.signOut();

            Intent intent = new Intent(TelaPrincipalActivity.this,
                    ValidarNumeroActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abrirConfiguracoes() {
        startActivity(new Intent(TelaPrincipalActivity.this,
                ConfiguracoesActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}