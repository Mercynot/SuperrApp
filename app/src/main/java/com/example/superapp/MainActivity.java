package com.example.superapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsuario;
    private Button btnLogout, btnAgregarProducto;
    private ListView lvProductos;
    private ProgressBar pbLista;

    private FirebaseAuth mAuth;
    private ProductoRepositorio repositorio;

    private final List<Producto> listaProductos = new ArrayList<>();
    private final List<String> listaTextos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        repositorio = new ProductoRepositorio();

        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) {
            irALogin();
            return;
        }

        tvUsuario = findViewById(R.id.tvUsuario);
        btnLogout = findViewById(R.id.btnLogout);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        lvProductos = findViewById(R.id.lvProductos);
        pbLista = findViewById(R.id.pbLista);

        String correo = usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "Usuario";
        tvUsuario.setText("Conectado: " + correo);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaTextos);
        lvProductos.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
            irALogin();
        });

        btnAgregarProducto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductoFormActivity.class);
            startActivity(intent);
        });

        lvProductos.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= listaProductos.size()) {
                return;
            }
            Producto seleccionado = listaProductos.get(position);
            Intent intent = new Intent(MainActivity.this, ProductoFormActivity.class);
            intent.putExtra("producto_id", seleccionado.getId());
            intent.putExtra("producto_nombre", seleccionado.getNombre());
            intent.putExtra("producto_categoria", seleccionado.getCategoria());
            intent.putExtra("producto_precio", seleccionado.getPrecio());
            intent.putExtra("producto_stock", seleccionado.getStock());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            cargarProductos();
        }
    }

    private void cargarProductos() {
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) return;

        pbLista.setVisibility(View.VISIBLE);

        repositorio.obtenerProductosPorUsuario(usuarioActual.getUid())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbLista.setVisibility(View.GONE);
                    listaProductos.clear();
                    listaTextos.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Producto p = doc.toObject(Producto.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaProductos.add(p);
                            String item = p.getNombre() + " (" + p.getCategoria() + ")\n"
                                    + "Precio: $" + p.getPrecio() + " | Stock: " + p.getStock();
                            listaTextos.add(item);
                        }
                    }

                    if (listaTextos.isEmpty()) {
                        listaTextos.add("No hay productos registrados todavía.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    pbLista.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}