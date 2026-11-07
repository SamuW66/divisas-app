package com.example.divisas.data.repository

import com.example.divisas.data.model.Role
import com.example.divisas.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("No se pudo crear el usuario")
        val profile = UserProfile(uid = uid, email = email, name = name, role = Role.USER)
        db.collection("users").document(uid).set(profile).await()
    }

    suspend fun login(email: String, password: String): UserProfile {
        auth.signInWithEmailAndPassword(email, password).await()
        return getProfile()
    }

    suspend fun getProfile(): UserProfile {
        val uid = currentUid ?: error("No hay sesion activa")
        return db.collection("users").document(uid).get().await()
            .toObject(UserProfile::class.java)
            ?: error("El perfil no existe en Firestore")
    }

    suspend fun updatePhoto(url: String) {
        val uid = currentUid ?: error("No hay sesion activa")
        db.collection("users").document(uid).update("photoUrl", url).await()
    }

    fun logout() = auth.signOut()
}
