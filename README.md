# RMA_DZ5
Pri pokretanju aplikacije učitava se početni zaslon (activity) koij se sastoji od button-a, textview-a i map fragmenta. Ukoliko je aplikacija prvi put pokrenuta 
pri pokretanju se traži dopuštenje za pristup lokaciji. U textview-a se ispisuju geografska dužina i širina, mjesto,adresa i država. Na karti se prikazuje trenutna
lokacija označena markerom. Klikom na mapu postavlja se marker uz zvučni signal. Klikom na button pita se za dopuštenje korištenja kamere(ukoliko se pokreće 
prvi put) te se otvara kamera. Nakon slikanja i dopuštenja pisanja na uređaj, slika se sprema u Pictures mapu unutar memorije uređaja, šalje se notifikacija o 
spremanju. Klikom na notifikaciju otvara se spremljena slika. Kamera je pokrenuta korištenjem funkcije startActivityForResult, nakon slikanja u funkciji 
onActivityResult uslikana slika u obliku data Intent-a sprema se u Bitmap format koji se kasnije kompresira i sprema u novi direktorij Pictures. Za pristup
lokaciji, kameri i pisanju na uređaj potrebno je osim dopuštenja u manifestu, zatražiti i dopuštenje od korisnika tijekom izvođenja aplikacije. Poteškoće su
se pojavile pri spremanju slike na uređaj jer je dopuštenje bilo definirano samo u manifestu, no problem je rješen definiranjem dopuštenja unutar MainActivity-ja.

Izvori:
[1]https://www.tutorialspoint.com/android/android_camera.htm 									-kamera
[2]https://developer.android.com/guide/topics/media/camera.html#camera-apps 					-kamera
[3]https://www.youtube.com/watch?v=ondCeqlAwEI 													-slikanje
[4]http://stackoverflow.com/questions/649154/save-bitmap-to-location							-spremanje
[5]http://stackoverflow.com/questions/37557709/how-to-create-a-new-folder-in-android-dcim		-spremanje, dopuštenja
[6]http://stackoverflow.com/questions/23123767/notification-pressed-to-open-up-file-in-default-app-android	-notifikacija
[7]http://stackoverflow.com/questions/35890257/android-errorexecution-failed-for-task-apptransformclasseswithdexforrelease

  

	