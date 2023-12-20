# WMS (Warehouse Management System) - Vistony
## Descripción
WMS es una aplicación de gestión de almacén desarrollada para la empresa Vistony. La aplicación se centra en facilitar el movimiento de inventario entre almacenes, camiones de entrega, ingresos y traslados, permitiendo una gestión eficiente de los productos y su ubicación en tiempo real. La aplicación está construida con Jetpack Compose y sigue una arquitectura MVVM. Además, integra consultas mediante Retrofit, Realm y Atlas MongoDB para proporcionar una experiencia de usuario eficiente y escalable.

#  Características Principales
## Movimiento de Inventario: 
Gestiona los movimientos de inventario entre almacenes, camiones de entrega y traslados.

## Ubicación en Tiempo Real: 
Permite un seguimiento en tiempo real de la ubicación y disponibilidad de productos.

## Integración con MongoDB Atlas: 
Utiliza MongoDB Atlas para almacenamiento de datos escalable y flexible.

## Base de Datos Realm: 
Sincronización eficiente de datos entre dispositivos y almacenamiento local con Realm.

## Selección de Base de Datos por País: 
Puede alternar entre bases de datos específicas para diferentes países (Perú, Chile, Paraguay, España, Bolivia, Marruecos, Ecuador e India).

## Diseño Moderno con Jetpack Compose: 
La interfaz de usuario utiliza Jetpack Compose para proporcionar un diseño moderno y fluido.

# Tecnologías Utilizadas
* Jetpack Compose
* Retrofit para consultas a API
* Arquitectura MVVM
* Realm para almacenamiento local
* MongoDB Atlas para almacenamiento remoto escalable

# Instalación
## Clonar el Repositorio:

'''
git clone https://github.com/TuUsuario/WMS.git
'''

## Configuración del Proyecto:
* Cambiar la ruta de la key
* Cambiar las direcciones de las api
* Cambiar la version de la db realm
## Ejecución:

cd WMS
./gradlew :app:run  # o comando específico para ejecutar la aplicación
Configuración y Uso
Configuración de la Base de Datos:

[Instrucciones para configurar la conexión con MongoDB Atlas]
Selección de Base de Datos por País:

[Instrucciones sobre cómo cambiar y configurar la base de datos según el país]
Uso de la Aplicación:

[Instrucciones detalladas sobre cómo utilizar las funciones principales de la aplicación]
Contribuciones
Agradecemos las contribuciones. Si deseas contribuir, sigue estos pasos:

Crea una rama con tu nombre/descripción de la característica:

bash
Copy code
git checkout -b feature/tu-nueva-caracteristica
Haz tus cambios y realiza un commit:

bash
Copy code
git add .
git commit -m "Añadir tu-nueva-caracteristica"
Sube tu rama:

bash
Copy code
git push origin feature/tu-nueva-caracteristica
Crea una solicitud de extracción (Pull Request) en GitHub.

Licencia
Este proyecto está bajo la Licencia [Nombre de la Licencia]. Consulta el archivo LICENSE para obtener más detalles.

Personaliza este README según las necesidades específicas de tu proyecto, proporcionando detalles adicionales sobre la configuración, uso y contribuciones.
