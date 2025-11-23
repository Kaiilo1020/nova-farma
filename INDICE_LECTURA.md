# 📖 Índice de Lectura - Nova Farma

**Guía de lectura recomendada para entender y trabajar con el proyecto.**

---

## 🎯 Orden Recomendado de Lectura

### **1. README.md** ⭐ (Empezar aquí)
**¿Qué es?** Resumen general del proyecto  
**¿Cuándo leerlo?** Primero, para entender qué es el sistema  
**Tiempo estimado:** 5-10 minutos

**Contenido:**
- Descripción del sistema
- Funcionalidades principales
- Estructura del código
- Requisitos cumplidos

---

### **2. INICIO_RAPIDO.md** ⚡
**¿Qué es?** Guía ultra-rápida para ejecutar el proyecto  
**¿Cuándo leerlo?** Si solo quieres ejecutarlo rápido sin leer todo  
**Tiempo estimado:** 2-3 minutos

**Contenido:**
- Requisitos mínimos
- Instalación rápida
- Comandos básicos
- Usuarios de prueba

**⚠️ Nota:** Si tienes problemas, ve al paso 3.

---

### **3. GUIA_INSTALACION_COMPLETA.md** 📦
**¿Qué es?** Guía paso a paso completa de instalación  
**¿Cuándo leerlo?** Si es tu primera vez instalando o si tienes problemas  
**Tiempo estimado:** 15-20 minutos

**Contenido:**
- Instalación de Java, PostgreSQL, Driver JDBC
- Configuración de base de datos (con SQL completo)
- Configuración del proyecto
- Solución de problemas comunes
- Checklist de verificación

---

---

### **5. ARQUITECTURA.md** 🏗️
**¿Qué es?** Documentación técnica de la estructura del código  
**¿Cuándo leerlo?** Cuando quieras entender cómo está organizado el código  
**Tiempo estimado:** 20-30 minutos

**Contenido:**
- Arquitectura en capas (Model → DAO → Service → UI)
- Flujos de seguridad (SHA-256)
- Control de roles (RBAC)
- Modelo de base de datos
- Diagramas y explicaciones técnicas

**👨‍💻 Recomendado para:** Desarrolladores que van a modificar el código

---

### **6. MODULO_VENTAS.md** 🛒
**¿Qué es?** Documentación específica del módulo de ventas  
**¿Cuándo leerlo?** Si necesitas entender cómo funciona el sistema de ventas  
**Tiempo estimado:** 10-15 minutos

**Contenido:**
- Interfaz del módulo de ventas
- Flujo de ventas
- Validaciones implementadas
- Integración con base de datos

---

### **7. PRUEBAS.md** 🧪
**¿Qué es?** Guía de pruebas y casos de uso  
**¿Cuándo leerlo?** Para probar todas las funcionalidades del sistema  
**Tiempo estimado:** 15-20 minutos

**Contenido:**
- Casos de prueba
- Escenarios de uso
- Validaciones a verificar

---

### **8. FAQ.md** ❓
**¿Qué es?** Preguntas frecuentes y respuestas objetivas  
**¿Cuándo leerlo?** Cuando tengas dudas sobre herramientas, scripts, o comportamiento del sistema  
**Tiempo estimado:** 5-10 minutos

**Contenido:**
- ¿Para qué sirven compile.bat/compile.sh?
- ¿TestPassword.class es necesario?
- ¿Por qué hay saltos en los IDs?
- Problemas comunes y soluciones

---

---

## 🗺️ Mapa de Lectura Visual

```
┌─────────────────────────────────────────────────────────┐
│                    ¿NUEVO EN EL PROYECTO?                 │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ↓
            ┌───────────────────────┐
            │   1. README.md         │ ← Empieza aquí
            │   (Resumen general)    │
            └───────────┬───────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
        ↓                               ↓
┌───────────────┐            ┌──────────────────────┐
│ 2. INICIO_    │            │ 3. GUIA_INSTALACION_  │
│    RAPIDO.md  │            │    COMPLETA.md        │
│ (Ejecutar     │            │ (Instalación paso a   │
│  rápido)      │            │  paso)                │
└───────┬───────┘            └───────────┬──────────┘
        │                               │
        └───────────────┬───────────────┘
                        │
                        ↓
        ┌───────────────┴───────────────┐
        │                               │
        ↓                               ↓
┌───────────────┐            ┌──────────────────────┐
│ 4. ARQUITEC-  │            │ 5. MODULO_VENTAS.md │
│    TURA.md    │            │ (Módulo específico)  │
│ (Estructura   │            └──────────────────────┘
│  del código)  │
└───────┬───────┘
        │
        ↓
┌───────────────┐
│ 6. PRUEBAS.md │
│ (Casos de uso)│
└───────────────┘
```

---

## 📋 Rutas de Lectura por Objetivo

### **Ruta 1: Solo Ejecutar el Proyecto** ⚡
1. `README.md` (5 min)
2. `INICIO_RAPIDO.md` (3 min)
3. Si hay problemas → `GUIA_INSTALACION_COMPLETA.md`

**Tiempo total:** ~10 minutos

---

### **Ruta 2: Instalar desde Cero** 📦
1. `README.md` (5 min)
2. `GUIA_INSTALACION_COMPLETA.md` (20 min)

**Tiempo total:** ~25 minutos

---

### **Ruta 3: Entender el Código** 👨‍💻
1. `README.md` (5 min)
2. `ARQUITECTURA.md` (30 min)
3. `MODULO_VENTAS.md` (15 min) - Si trabajas en ventas
4. `PRUEBAS.md` (15 min) - Para entender casos de uso
5. `FAQ.md` (5 min) - Si tienes dudas técnicas

**Tiempo total:** ~1 hora

---

### **Ruta 4: Documentación Completa** 📚
1. `README.md`
2. `INICIO_RAPIDO.md`
3. `GUIA_INSTALACION_COMPLETA.md`
4. `ARQUITECTURA.md`
5. `MODULO_VENTAS.md`
6. `PRUEBAS.md`
7. `FAQ.md`

**Tiempo total:** ~1.5 horas

---

## ❓ Preguntas Frecuentes

### **¿Puedo saltarme algunos archivos?**
- ✅ Sí, depende de tu objetivo (ver rutas arriba)
- ⚠️ **NO te saltes:** `README.md` (siempre léelo primero)

### **¿Qué archivo leo si tengo un error?**
1. `GUIA_INSTALACION_COMPLETA.md` → Sección "Solución de Problemas"
2. `FAQ.md` → Sección "Problemas Comunes"

### **¿Dónde está el código SQL para crear las tablas?**
- `GUIA_INSTALACION_COMPLETA.md` → Sección 3.2

### **¿Cómo entiendo la estructura del código?**
- `ARQUITECTURA.md` → Toda la explicación técnica

### **¿Para qué sirven compile.bat y compile.sh?**
- `FAQ.md` → Sección "Herramientas y Scripts"

---

## 🎯 Recomendación Final

**Para la mayoría de usuarios:**
1. `README.md` ← **EMPIEZA AQUÍ**
2. `INICIO_RAPIDO.md` o `GUIA_INSTALACION_COMPLETA.md`
3. Si vas a modificar código → `ARQUITECTURA.md`

**¡Listo para empezar! 🚀**

