# NexWork – Marketplace de Servicios para Android

![Estado del Proyecto](https://img.shields.io/badge/estado-en%20desarrollo-blue)
![Android](https://img.shields.io/badge/Android-API%2029%20--%2036-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-orange)
![Firebase](https://img.shields.io/badge/Firebase-Backend-yellow)
![Arquitectura](https://img.shields.io/badge/Arquitectura-MVVM-lightgrey)

---

## 📚 Tabla de Contenido

- [Descripción General](#descripción-general)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Funcionalidades Principales](#funcionalidades-principales)
  - [Para Clientes](#para-clientes)
  - [Para Proveedores](#para-proveedores)
  - [Para Administradores](#para-administradores)
- [Características Principales](#funcionalidades-clave)
- [Estructura del Proyecto](#estructura-del-proyecto)
  - [Búsqueda de Servicios](#búsqueda-de-servicios)
  - [Perfiles de Usuario](#perfiles-de-usuario)
  - [Mensajería](#mensajería)
  - [Reseñas](#reseñas)
- [Notas](#notas)

---

## 🚀Descripción General

NexWork es una aplicación móvil para Android que conecta proveedores de servicios con clientes dentro de un marketplace de dos vías. La plataforma facilita la búsqueda, reserva y gestión de servicios mediante un sistema basado en roles para clientes, proveedores y administradores.

**Flujo de Navegación**

1. Inicio de la Aplicación (Splash)
- La app inicia mostrando una pantalla que verifica si el usuario tiene sesión activa.  
- Si está autenticado, avanza; si no, continúa al flujo de bienvenida.

2. Rutas de Autenticación
- **Si no está autenticado:** se muestra la pantalla de bienvenida para registrarse o iniciar sesión.  
- **Si está autenticado:** se consulta su rol en la base de datos y se redirige directamente a la pantalla principal según ese rol.

3. Proceso de Login
La pantalla de acceso permite:
- Email y contraseña  
- Inicio con Google  
- Modo invitado (sin registro)

4. Navegación Basada en Roles:
   
 **Cliente:**
- Home con carrusel y categorías  
- Pestaña de categorías  
- Mensajes habilitados  
- Perfil con favoritos y pedidos  

**Proveedor:**
- Dashboard de proveedor  
- Sin pestaña de categorías  
- Notificaciones activadas  
- Perfil para gestión de servicios  

 **Administrador:**
- Acceso completo a mensajes y notificaciones  
- Sin pestaña de categorías  
- Herramientas para administración de usuarios y categorías  

**Invitado:**
- Acceso limitado a Home, Categorías y un Perfil básico  
- Sin mensajes ni notificaciones  

5. Registro de Usuarios
- El usuario elige rol (cliente o proveedor).  
- Una vez registrado, es enviado directamente a la Home configurada según su rol.


---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Icono | Descripción |
|-----------|-------|-------------|
| **Android** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/android/android-original.svg" width="32"/> | Plataforma (API 29–36) |
| **Kotlin** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg" width="32"/> | Lenguaje Kotlin 2.0.21 |
| **MVVM + Repository** | <img src="https://img.shields.io/badge/Pattern-MVVM-blue?style=flat-square"/> | Arquitectura principal del proyecto |
| **Firebase** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/firebase/firebase-plain.svg" width="32"/> | Auth, Firestore, Storage, Analytics |
| **Material Design** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/materialui/materialui-original.svg" width="32"/> | Diseño UI |
| **Glide** | <img src="https://github.com/bumptech/glide/blob/master/static/glide_logo.png?raw=true" width="32"/> | Carga y manejo de imágenes |
| **Jetpack Navigation** | <img src="https://developer.android.com/images/jetpack/jetpack-navigation-icon.svg" width="32"/> | Navegación entre pantallas |

---

## 🚀 Funcionalidades Principales

### Para Clientes
- Exploración de servicios por categorías o mediante búsqueda.
- Sistema de favoritos para guardar servicios.
- Gestión de órdenes: creación y seguimiento.
- Mensajería en tiempo real con proveedores.

### Para Proveedores
- Gestión de servicios con hasta tres planes.
- Planes flexibles: Sencillo, Básico y Premium, con complementos personalizados.
- Panel del proveedor para disponibilidad y pedidos.
- Galería con un máximo de tres imágenes por servicio.

### Para Administradores
- Gestión de usuarios.
- Supervisión de servicios.
- Administración de categorías.

---

## ✨ Características Principales

### Búsqueda de Servicios
- Navegación por categorías.
- Búsqueda en tiempo real.
- Carrusel de destacados.
- Recomendaciones de servicios similares.

### Perfiles de Usuario
- Datos personales editables.
- Foto de perfil.
- Cambio de rol entre cliente y proveedor.
- Gestión de seguridad de cuenta.

### Mensajería
- Chat en tiempo real.
- Historial persistente.
- Búsqueda de usuarios para iniciar conversación.

### Reseñas 
- Crear, leer, actualizar y eliminar reseñas.
- Listado de reseñas por servicio.

> ⚠️ **Estado de la funcionalidad de Reseñas**  
> Falta implementación completa. Solo existen el **model** y el **repository**.

---

## 🧱 Estructura del Proyecto
A continuación, se detalla la organización de los archivos y directorios principales del proyecto, siguiendo una estructura modular y basada en componentes:

```
app/  
├── src/main/  
│   ├── java/com/example/nexwork/  
│   │   ├── data/  
│   │   │   ├── model/          # Data models (User, Service, Category, etc.)  
│   │   │   └── repository/     # Firebase data access layer  
│   │   └── ui/  
│   │       ├── auth/           # Authentication flows  
│   │       ├── home/           # Home screens  
│   │       ├── services/       # Service discovery and management  
│   │       ├── categories/     # Category browsing  
│   │       ├── profile/        # User profile and account management  
│   │       └── chat/           # Messaging system  
│   └── res/  
│       ├── layout/             # XML layouts  
│       └── values/             # Strings, colors, themes  
```

---

## 🔔 Notas
- La app está en versión 0.1.
- Interfaz completamente en español.
- Funciones orientadas a servicios con visita a domicilio también integradas.

---

## 👨‍💻 Autores

**NexWork** fue desarrollado por **Miguel Angel Sepulveda Burgos** e **Inder Arbey Gutiérrez Sandoval**.

*   <img src="https://cdn.worldvectorlogo.com/logos/github-icon-2.svg" width="20" height="20"/> GitHub: [@moonthang](https://github.com/moonthang)
*   <img src="https://static.vecteezy.com/system/resources/previews/018/930/480/non_2x/linkedin-logo-linkedin-icon-transparent-free-png.png" width="20" height="20"/> LinkedIn: [Miguel Ángel Sepulveda Burgos](https://www.linkedin.com/in/miguel-%C3%A1ngel-sep%C3%BAlveda-burgos-a87808167/)

*   <img src="https://cdn.worldvectorlogo.com/logos/github-icon-2.svg" width="20" height="20"/> GitHub: [@igutisan](https://github.com/igutisan)
