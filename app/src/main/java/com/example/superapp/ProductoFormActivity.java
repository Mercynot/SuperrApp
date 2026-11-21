package com.example.superapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProductoFormActivity extends AppCompatActivity {

    private TextView tvTituloForm;
    private EditText etNombre, etCategoria, etPrecio, etStock;
    private Button btnGuardar, btnEliminar, btnVolver;
    private ProgressBar pbCargando;

    private ProductoRepositorio repositorio;
    private FirebaseAuth mAuth;

    private String productoIdEditar = null;
    private boolean estaCargando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);

        repositorio = new ProductoRepositorio();
        mAuth = FirebaseAuth.getInstance();

        tvTituloForm = findViewById(R.id.tvTituloForm);
        etNombre = findViewById(R.id.etNombre);
        etCategoria = findViewById(R.id.etCategoria);
        etPrecio = findViewById(R.id.etPrecio);
        etStock = findViewById(R.id.etStock);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);
        pbCargando = findViewById(R.id.pbCargando);

        if (getIntent() != null && getIntent().hasExtra("producto_id")) {
            productoIdEditar = getIntent().getStringExtra("producto_id");
            tvTituloForm.setText("Editar producto");
            btnGuardar.setText("Actualizar producto");
            btnEliminar.setVisibility(View.VISIBLE);

            etNombre.setText(getIntent().getStringExtra("producto_nombre"));
            etCategoria.setText(getIntent().getStringExtra("producto_categoria"));
            etPrecio.setText(String.valueOf(getIntent().getIntExtra("producto_precio", 0)));
            etStock.setText(String.valueOf(getIntent().getIntExtra("producto_stock", 0)));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!estaCargando) {
                    finish();
                } else {
                    Toast.makeText(ProductoFormActivity.this, "Espera a que termine la operación...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGuardar.setOnClickListener(v -> guardarOActualizar());
        btnEliminar.setOnClickListener(v -> eliminar());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void setCargando(boolean cargando) {
        estaCargando = cargando;
        pbCargando.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
        btnEliminar.setEnabled(!cargando);
        btnVolver.setEnabled(!cargando);
        etNombre.setEnabled(!cargando);
        etCategoria.setEnabled(!cargando);
        etPrecio.setEnabled(!cargando);
        etStock.setEnabled(!cargando);
    }

    private void guardarOActualizar() {
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }
        if (nombre.length() < 2) {
            etNombre.setError("Ingresa un nombre de al menos 2 caracteres");
            etNombre.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(categoria)) {
            etCategoria.setError("La categoría es obligatoria");
            etCategoria.requestFocus();
            return;
        }
        if (categoria.length() < 2) {
            etCategoria.setError("Ingresa una categoría válida");
            etCategoria.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(precioStr)) {
            etPrecio.setError("El precio es obligatorio");
            etPrecio.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(stockStr)) {
            etStock.setError("El stock es obligatorio");
            etStock.requestFocus();
            return;
        }

        int precio;
        int stock;
        try {
            precio = Integer.parseInt(precioStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa números válidos en precio y stock", Toast.LENGTH_SHORT).show();
            return;
        }

        if (precio <= 0) {
            etPrecio.setError("El precio debe ser mayor a 0");
            etPrecio.requestFocus();
            return;
        }
        if (stock < 0) {
            etStock.setError("El stock no puede ser negativo");
            etStock.requestFocus();
            return;
        }

        String idFinal = (productoIdEditar != null) ? productoIdEditar : repositorio.generarNuevoId();
        Producto producto = new Producto(idFinal, nombre, categoria, precio, stock, usuario.getUid());

        setCargando(true);
        Toast.makeText(this, "Guardando en Firestore...", Toast.LENGTH_SHORT).show();

        repositorio.guardarProducto(producto)
                .addOnSuccessListener(unused -> {
                    setCargando(false);
                    String msg = (productoIdEditar != null)
                            ? "Producto actualizado correctamente"
                            : "Producto agregado correctamente";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setCargando(false);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void eliminar() {
        if (productoIdEditar == null) return;

        setCargando(true);
        Toast.makeText(this, "Eliminando producto...", Toast.LENGTH_SHORT).show();

        repositorio.eliminarProducto(productoIdEditar)
                .addOnSuccessListener(unused -> {
                    setCargando(false);
                    Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setCargando(false);
                    Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}