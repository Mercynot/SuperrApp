package com.example.superapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProductoRepositorio {

    private static final String COLLECTION_NAME = "productos";
    private final FirebaseFirestore db;

    public ProductoRepositorio() {
        this.db = FirebaseFirestore.getInstance();
    }

    public String generarNuevoId() {
        return db.collection(COLLECTION_NAME).document().getId();
    }

    public Task<Void> guardarProducto(Producto producto) {
        return db.collection(COLLECTION_NAME)
                .document(producto.getId())
                .set(producto);
    }

    public Task<QuerySnapshot> obtenerProductosPorUsuario(String userId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();
    }

    public Task<Void> eliminarProducto(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete();
    }
}