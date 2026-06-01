# Seguros Mini

Version reducida de un sistema de gestion de seguros hecha en Java Swing.
La aplicacion toma como inspiracion el proyecto `seguros-tauri-reference`, pero
mantiene solo el flujo esencial para una entrega escolar de POO.

## Ejecutar

1. Abre una terminal en esta carpeta.
2. Ejecuta `ejecutar.bat`.

El script busca `javac` en el `PATH`. En este equipo tambien intenta usar el JDK
incluido con Minecraft Launcher cuando no encuentra un JDK instalado globalmente.

Para verificar persistencia, reportes y respaldos sin abrir la interfaz ejecuta
`probar.bat`.

## Funciones

- Dashboard con alertas de primas vencidas y proximas a vencer.
- Registro de clientes.
- Registro de polizas de auto, gastos medicos y vida.
- Registro automatico de una prima por cada poliza.
- Marcado de primas como pagadas.
- Base de datos en `data/clientes.txt`, `data/polizas.txt` y `data/recibos.txt`.
- Reportes `.txt` y respaldos binarios `.bin` en `data/respaldos`.
- Respaldo automatico cada 60 segundos mediante un hilo programado.

## Conceptos POO incluidos

- Clases y objetos: `Cliente`, `Recibo`, `SeguroService`.
- Encapsulamiento: atributos privados con metodos publicos.
- Herencia: `PolizaAuto`, `PolizaVida` y `PolizaGastosMedicos` heredan de `Poliza`.
- Polimorfismo: la interfaz recorre una coleccion de `Poliza` y llama `getTipo()` y
  `getDetalle()` sin conocer la subclase concreta.
- Colecciones dinamicas: uso de `ArrayList`.
- Archivos de texto y binarios: `TextDatabase`.
- Hilos y concurrencia: `ScheduledExecutorService` en `SeguroService`.
- Interfaz grafica: `MainFrame` extiende `JFrame`.

## Estructura

```text
src/seguros/
  App.java
  model/
  persistence/
  service/
  ui/
```
