# ❓ Preguntas Frecuentes - Nova Farma

Preguntas comunes sobre el proyecto y sus herramientas.

---

## 🔧 Herramientas y Scripts

### **¿Para qué sirven `compile.bat` y `compile.sh` si puedo ejecutar desde el IDE?**

**Respuesta objetiva:**

| Escenario | Usar Scripts | Usar IDE |
|-----------|--------------|----------|
| **Compilar desde terminal** | ✅ Sí, necesario | ❌ No funciona |
| **Ejecutar desde terminal** | ✅ Sí, necesario | ❌ No funciona |
| **Desarrollo normal** | ⚠️ Opcional | ✅ Recomendado |
| **Demostración/Presentación** | ✅ Útil (más profesional) | ✅ También funciona |
| **CI/CD o automatización** | ✅ Necesario | ❌ No aplica |

**Explicación:**

1. **Los scripts (`compile.bat` / `compile.sh`):**
   - Compilan el proyecto desde la terminal/consola
   - Útiles si no tienes IDE instalado
   - Útiles para automatización (scripts, CI/CD)
   - Útiles para demostraciones (muestra que sabes compilar desde terminal)
   - Configuran el classpath correctamente

2. **El IDE (Cursor/VS Code/IntelliJ):**
   - Compila automáticamente cuando guardas
   - Más cómodo para desarrollo diario
   - Tiene autocompletado y debugging
   - Pero requiere que configures el Build Path manualmente

**Conclusión:**
- ✅ **Para desarrollo diario:** Usa el IDE (más cómodo)
- ✅ **Para presentar/demostrar:** Los scripts muestran que sabes compilar desde terminal
- ✅ **Puedes tener ambos:** No son excluyentes

**¿Puedo eliminarlos?**
- ⚠️ **No recomendado:** Son útiles para otros compañeros que no usen IDE
- ✅ **Sí puedes:** Si solo trabajas tú y siempre usas IDE, puedes eliminarlos
- 💡 **Recomendación:** Déjalos, ocupan poco espacio y pueden ayudar a otros

---

### **¿`TestPassword.class` me sirve para algo?**

**Respuesta objetiva:**

| Pregunta | Respuesta |
|----------|-----------|
| **¿Qué es?** | Archivo compilado (bytecode) de una clase Java de prueba |
| **¿Es necesario?** | ❌ No, es un archivo de prueba |
| **¿Puedo eliminarlo?** | ✅ Sí, sin problemas |
| **¿Afecta el proyecto?** | ❌ No, no se usa en el código |

**Explicación:**

- `TestPassword.class` es un archivo `.class` (bytecode compilado)
- Probablemente fue creado para probar la encriptación SHA-256
- **No es parte del proyecto principal**
- Los archivos `.class` se generan al compilar y no deben estar en el repositorio
- Si necesitas probar la encriptación, puedes ejecutar `SecurityHelper.java` directamente

**Recomendación:**
- ✅ **Elimínalo:** No es necesario y puede confundir
- ✅ **Si necesitas probar:** Usa `SecurityHelper.java` que tiene un método `main()` para pruebas

---

## 📚 Documentación

### **¿En qué orden debo leer los archivos .md?**

**Respuesta:** Lee [`INDICE_LECTURA.md`](INDICE_LECTURA.md)

**Resumen rápido:**
1. `README.md` - Empieza aquí
2. `INICIO_RAPIDO.md` o `GUIA_INSTALACION_COMPLETA.md`
3. `ARQUITECTURA.md` - Si vas a modificar código

---

## 🗄️ Base de Datos

### **¿Por qué hay saltos en los IDs de usuarios (1, 2, 5, 6, 7)?**

**Respuesta:** Es comportamiento normal de PostgreSQL con `SERIAL`.

- Los IDs no se reutilizan automáticamente
- Si eliminas un usuario con ID 5, ese ID queda "libre" pero no se reutiliza
- El siguiente usuario tendrá ID 8 (no 5)
- **No es un problema**, es el comportamiento esperado

**Más detalles:** Ver `README.md` → Sección "NOTAS IMPORTANTES"

---

### **¿Puedo eliminar un usuario que tiene ventas?**

**Respuesta:** No, por diseño del sistema.

- El sistema **no permite** eliminar usuarios con ventas registradas
- Esto es para **conservar el historial** del negocio
- Si un trabajador ya no trabaja, simplemente no le permitas iniciar sesión
- Las ventas deben mantener la referencia al usuario que las hizo

**Alternativa futura:** Implementar un campo `activo` para desactivar usuarios sin eliminarlos.

---

## 💻 Desarrollo

### **¿Puedo ejecutar el proyecto sin compilar primero?**

**Respuesta:** Depende de cómo lo ejecutes.

| Método | ¿Necesita compilar? |
|--------|---------------------|
| **IDE (Run/Debug)** | ❌ No, compila automáticamente |
| **Terminal (`java`)** | ✅ Sí, necesitas compilar primero |
| **Scripts (`compile.bat`)** | ✅ Sí, pero el script lo hace por ti |

**Recomendación:** Usa el IDE para desarrollo, es más cómodo.

---

### **¿Necesito instalar algo además de Java y PostgreSQL?**

**Respuesta:** Solo necesitas:

1. ✅ **Java JDK 8+** (incluye `javac` para compilar)
2. ✅ **PostgreSQL 12+**
3. ✅ **Driver JDBC** (`postgresql-42.7.8.jar` en `lib/`)

**No necesitas:**
- ❌ Maven/Gradle (el proyecto no los usa)
- ❌ Servidor de aplicaciones (es aplicación de escritorio)
- ❌ Framework adicional (Java Swing está incluido en JDK)

---

## 🔐 Seguridad

### **¿Por qué se usa SHA-256 y no algo más seguro como BCrypt?**

**Respuesta:** Por simplicidad y requisitos del proyecto.

- SHA-256 es más simple de implementar y entender
- BCrypt requiere librerías externas
- Para un proyecto académico, SHA-256 es suficiente
- **Nota:** En producción, se recomienda BCrypt con salting

**Más detalles:** Ver `ARQUITECTURA.md` → Sección "Escalabilidad Futura"

---

## 📁 Estructura del Proyecto

### **¿Por qué no hay carpeta `database/` con los scripts SQL?**

**Respuesta:** Fue eliminada para simplificar el proyecto.

- Los scripts SQL ahora están en la documentación (`GUIA_INSTALACION_COMPLETA.md`)
- Puedes crear las tablas desde pgAdmin o copiar el SQL de la documentación
- **Ventaja:** Menos archivos que mantener actualizados

---

### **¿Por qué no hay carpeta `bin/` con los archivos compilados?**

**Respuesta:** Los archivos `.class` no deben estar en el repositorio.

- Se generan automáticamente al compilar
- Cada desarrollador compila su propia versión
- **Ventaja:** El repositorio es más limpio y pequeño

---

## 🐛 Problemas Comunes

### **"Driver not found" o "ClassNotFoundException"**

**Solución:**
1. Verifica que `postgresql-42.7.8.jar` esté en `lib/`
2. Si usas IDE, agrega el JAR al Build Path
3. Si usas terminal, verifica el classpath: `-cp "lib/postgresql-42.7.8.jar"`

---

### **"Connection refused"**

**Solución:**
1. Verifica que PostgreSQL esté corriendo
2. **Windows:** Servicios → PostgreSQL → Iniciar
3. **Linux:** `sudo systemctl start postgresql`
4. Verifica el puerto (por defecto: 5432)

---

### **"Database nova_farma_db does not exist"**

**Solución:**
```sql
psql -U postgres
CREATE DATABASE nova_farma_db;
\q
```

Luego crea las tablas (ver `GUIA_INSTALACION_COMPLETA.md` → Sección 3.2)

---

## 📞 ¿No encuentras tu pregunta?

1. Revisa `INDICE_LECTURA.md` para encontrar la documentación relevante
2. Busca en los archivos `.md` con Ctrl+F
3. Revisa los comentarios en el código (están bien documentados)

---

**Última actualización:** Diciembre 2024

