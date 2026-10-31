AUTO TOUCH PRO — PROYECTO INICIAL ANDROID
=========================================

Configuración inicial:
- Velocidad: 120 clics por minuto (2 por segundo).
- Mira flotante arrastrable.
- Botones flotantes INICIAR / DETENER.
- Opciones iniciales: 60, 120, 200, 300, 500 y 1000 CPM.
- La aplicación no necesita Internet durante su uso.

IMPORTANTE:
Este archivo es el PROYECTO FUENTE, no un APK ya compilado.
El APK se genera con Android Gradle Plugin mediante GitHub Actions.

CÓMO OBTENER EL APK USANDO SOLO EL TELÉFONO:
1. Descarga y descomprime el ZIP.
2. Crea un repositorio en GitHub desde el navegador del teléfono.
3. Sube el contenido completo de la carpeta AutoTouchPro al repositorio.
4. Abre la pestaña Actions y ejecuta "Build AUTO TOUCH PRO APK"
   (o espera a que termine la compilación tras subir los archivos).
5. Entra al resultado de la ejecución y descarga el artefacto
   AUTO-TOUCH-PRO-debug. Dentro estará app-debug.apk.
6. Instala el APK. Android puede pedir autorización para instalar
   aplicaciones desde el navegador/gestor de archivos.

PRIMER USO:
1. Abre AUTO TOUCH PRO.
2. Concede "Permitir superposición".
3. En Ajustes > Accesibilidad, activa AUTO TOUCH PRO.
4. Vuelve a la app y pulsa MOSTRAR MIRA FLOTANTE.
5. Arrastra la mira al lugar donde quieras tocar.
6. Pulsa INICIAR en el panel flotante. DETENER interrumpe los toques.

SEGURIDAD Y LIMITACIONES:
- La mira representa el punto de toque: se pulsa el centro de la mira.
- No lee el contenido de otras aplicaciones.
- El servicio de accesibilidad solo se usa para ejecutar gestos solicitados
  explícitamente por el usuario.
- La velocidad real puede variar por la carga del teléfono y Android.
- 1000 CPM equivale a unos 16,7 toques por segundo; Android puede no
  aceptar todos los gestos a esa velocidad.
- Usa la herramienta respetando las reglas de cada aplicación.
- Este proyecto es una base inicial y debe probarse en el dispositivo.
