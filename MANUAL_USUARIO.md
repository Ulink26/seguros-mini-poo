# Manual de usuario

## Inicio

Ejecuta `ejecutar.bat`. En el primer inicio se crean datos de ejemplo y la
carpeta `data`.

## Flujo basico

1. Abre la pestana **Clientes** y registra una persona.
2. Abre **Polizas** y registra una poliza para ese cliente.
3. Revisa **Primas**. La aplicacion crea una prima al registrar la poliza.
4. Selecciona una prima y usa **Marcar como pagada** cuando corresponda.
5. Consulta **Resumen** para ver alertas y metricas.

## Reportes y respaldos

En **Resumen** puedes generar un reporte TXT o un respaldo BIN. Ademas, el
sistema genera un respaldo binario cada 60 segundos mientras permanece abierto.

## Archivos de datos

- `data/clientes.txt`
- `data/polizas.txt`
- `data/recibos.txt`
- `data/respaldos/*.bin`
- `data/respaldos/*.txt`

Los archivos TXT son la base de datos principal. Se pueden revisar con un editor
de texto, pero conviene modificarlos desde la aplicacion para evitar errores de
formato.
