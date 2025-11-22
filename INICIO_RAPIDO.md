# ⚡ Inicio Rápido - Nova Farma

Guía ultra-resumida para ejecutar el proyecto en 5 minutos.

---

## 🚀 Requisitos

- Java JDK 8+
- PostgreSQL 12+
- Driver JDBC: `postgresql-42.7.1.jar`

---

## 📥 Instalación Rápida

### 1. PostgreSQL

```bash
# Conectar
psql -U postgres

# Crear BD y ejecutar script
CREATE DATABASE nova_farma_db;
\c nova_farma_db
\i database/schema.sql
\q
```

### 2. Driver JDBC

Descargar: https://jdbc.postgresql.org/download/
Guardar en: `lib/postgresql-42.7.1.jar`

### 3. Configurar Contraseña

Editar `src/com/novafarma/util/DatabaseConnection.java`:

```java
private static final String DB_PASSWORD = "tu_password_real";
```

---

## ▶️ Ejecutar

### Windows

```cmd
compile.bat
```

### Linux/Mac

```bash
chmod +x compile.sh
./compile.sh
```

---

## 👤 Usuarios de Prueba

| Usuario      | Contraseña    | Rol           |
|--------------|---------------|---------------|
| admin        | admin123      | Administrador |
| trabajador1  | trabajador123 | Trabajador    |

---

## ✅ Verificación Rápida

1. **Login:** Probar con `admin` / `admin123`
2. **Ver inventario:** Pestaña "Inventario"
3. **Agregar producto:** Clic en "➕ Agregar Producto"
4. **Cambiar a trabajador:** Login con `trabajador1`
5. **Ver restricciones:** Botones deshabilitados

---

## 🔑 Características Clave

✅ Contraseñas encriptadas con SHA-256
✅ Recuperación de contraseña
✅ Control de roles estricto
✅ Gestión de inventario
✅ Sistema de ventas

---

## 🐛 Problemas Comunes

### "Driver not found"
→ Verifica que `postgresql-42.7.1.jar` esté en `lib/`

### "Connection refused"
→ Inicia PostgreSQL: `sudo service postgresql start`

### "Database not exist"
→ Ejecuta `database/schema.sql`

---

## 📚 Documentación Completa

- **Instalación detallada:** `INSTALACION.md`
- **Guía de pruebas:** `PRUEBAS.md`
- **README completo:** `README.md`

---

**¡Listo para demostrar! 🎯**

