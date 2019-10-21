# CocinaInteligente

Git para compartir el progreso de la aplicacion android y la red de sensores. 

Proyecto IoT, grado de Tecnologías Interactivas - UPV.

## Como usar GIT

Dentro de este repositorio, copias el enlace para su respectiva clonación y en el terminal (cmd), escribes: 

```
git clone <enlace>
```

Esto realizará una clonación del repositorio. Será necesario tener una cuenta de GitHub para realizar subidas al repositorio.
Dentro del directorio que se creará, con el respectivo repositorio, cuando se haya modificado de manera **correcta** un archivo, se ejecutarán los siguientes comandos.

```
git commit -a -m "AQUI VA UN RESUMEN CORTO DE LOS CAMBIOS"
git push origin master
```

Si en algún caso se ha añadido un archivo nuevo al directorio, para que este se actualize en el repositorio deberás añadirlo de la siguiente manera:

```
git add .
```

Para cancelar los cambios realizados en algun documento antes de su subida...

```
git checkout <archivo>
```

Si se quiere actualizar el repositorio con los cambios de otro colaborador, utilizar el comando pull:

```
git pull
```

Desde el siguiente [enlace](http://rogerdudler.github.io/git-guide/index.es.html) podrás encontrar una guía de uso mas extensa.
