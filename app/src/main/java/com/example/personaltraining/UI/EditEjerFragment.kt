package com.example.personaltraining.UI

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.EditEjerAdapter
import com.example.personaltraining.adapter.MediaAdapter
import com.example.personaltraining.appFiles.FileManager
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.EditEjerFragmentBinding
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.MediaTipo
import com.example.personaltraining.model.Media
import com.example.personaltraining.viewModel.EditEjerFragmentViewModel
import com.example.personaltraining.viewModel.EditEjerFragmentViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class EditEjerFragment : Fragment() {

    private val args: EditEjerFragmentArgs by navArgs()
    private lateinit var viewModel: EditEjerFragmentViewModel
    private lateinit var viewModelFactory: EditEjerFragmentViewModelFactory
    private lateinit var adapter: EditEjerAdapter
    private lateinit var fileManager: FileManager
    private lateinit var mediaAdapter: MediaAdapter

    private val TAG = "EditEjerFragment"
    private var negacion = false
    private var negacion2 = false
    private var rutinaID = 0
    private var ultimoFormato : MediaTipo? = null
    private var ejercicioSeleccionado: Ejercicio? = null

    private var _binding: EditEjerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (requireActivity().application as RutinasApplication).repository
        viewModelFactory = EditEjerFragmentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[EditEjerFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditEjerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí se inicia la solicitud de permisos
        requestPermissionsIfNeeded()

        fileManager = FileManager(requireContext())

        // Configurar el RecyclerView y el adaptador
        adapter = EditEjerAdapter { ejercicio ->
            showOptionsMenu(ejercicio)
        }
        binding.recyclerListaEjerciciosEdit.adapter = adapter
        binding.recyclerListaEjerciciosEdit.layoutManager = LinearLayoutManager(requireContext())

        //val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mediaAdapter = MediaAdapter { media ->
            showMediaOptionsMenu(media)
        }
        binding.viewPagerMedia.adapter = mediaAdapter

        checkStoragePermissions()

        viewModel.mediaList.observe(viewLifecycleOwner) { media ->
            //Log.d(TAG, "Lista de media actualizada: $media")
            mediaAdapter.submitList(media)
        }

        // Observa los LiveData del ViewModel
        viewModel.rutina.observe(viewLifecycleOwner) { rutina ->
            if (rutina != null) {
                // Maneja la rutina
                binding.edtNombreRutinaEdit.setText(rutina.nombre)
            } else {
                // Maneja el caso cuando la rutina no se encuentra, por ejemplo, mostrar un mensaje de error
                Toast.makeText(requireContext(), getString(R.string.routine_not_found), Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.currentEjercicio.observe(viewLifecycleOwner) { ejercicio ->
            val valor = ejercicio != null
            changeEnableFieldsEspecif(valor)
            //Log.d(TAG, "Ejercicio actual: $ejercicio")
        }

        viewModel.ejercicios.observe(viewLifecycleOwner) { ejercicios ->
            Log.d(TAG, "Lista de ejercicios actualizada: $ejercicios")
            adapter.submitList(ejercicios)
        }

        // Obtén la rutina y los ejercicios por el ID de la rutina
        if (args.ID != 0){
            viewModel.getRutinaById(args.ID)
            viewModel.getEjerciciosByRutinaId(args.ID)
            rutinaID = args.ID
        }

        binding.chkDurTiempoEdit.isChecked = true

        binding.btnCambiarNombre.setOnClickListener {
            val newName = binding.edtNombreRutinaEdit.text.toString()
            if (newName.isNotEmpty()) {
                val currentRutina = viewModel.rutina.value
                if (currentRutina != null) {
                    currentRutina.nombre = newName
                    //Log.d("EditEjerFragment", "Datos de la rutina actualizados: $currentRutina")
                    viewModel.updateRutina(currentRutina)
                    Toast.makeText(requireContext(),
                        getString(R.string.routine_name_changed), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.routine_not_found), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEditarEjercicio.setOnClickListener {
            if (areFieldsValid()) {
                val nombreEjercicio = binding.edtNombreEjercicioEdit.text.toString()
                val duracionEjercicio = darFormato(binding.edtimeDuracionEjercicioEdit.text.toString())
                val duracionDescanso = darFormato(binding.edtimeDuracionDescansoEdit.text.toString())
                val tipo = binding.chkRepeticionEdit.isChecked
                val objetivo = if (tipo) binding.edtCantRepEdit.text?.toString() else null
                val ejercicio = Ejercicio(
                    ID = ejercicioSeleccionado?.ID ?: 0,
                    Nombre = nombreEjercicio,
                    DEjercicio = duracionEjercicio,
                    DDescanso = duracionDescanso,
                    Objetivo = objetivo,
                    isObjetivo = tipo,
                    rutinaId = rutinaID
                )
                if (ejercicioSeleccionado != null ) {
                    viewModel.updateEjercicio(ejercicio)
                } else {
                    viewModel.addEjercicio(ejercicio)
                }

                clearFields()
                viewModel.cambiarEjercicioActual(null)
                changeEnableFieldsEspecif(false)
                ejercicioSeleccionado = null
                viewModel.getEjerciciosByRutinaId(rutinaID)
                viewModel.loadMediaForCurrentExercise(null)
                Toast.makeText(requireContext(),
                    getString(R.string.added_exercise), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.you_must_fill_out_all_fields_to_enter_an_exercise), Toast.LENGTH_SHORT).show()
            }
        }

        // Agregar TextWatcher a los EditText
        addTimeTextWatcher(binding.edtimeDuracionEjercicioEdit)
        addTimeTextWatcher(binding.edtimeDuracionDescansoEdit)
        addNumberTextWatcher(binding.edtCantRepEdit)

        binding.chkRepeticionEdit.setOnClickListener {
            val isChecked = binding.chkRepeticionEdit.isChecked
            binding.chkDurTiempoEdit.isChecked = !isChecked
            binding.edtCantRepEdit.isEnabled = isChecked
            binding.edtimeDuracionEjercicioEdit.hint = if (isChecked)
                getString(R.string.estimation_of_the_exercise) else
                getString(R.string.duration_of_exercise)
        }
        binding.chkDurTiempoEdit.setOnClickListener {
            binding.chkRepeticionEdit.isChecked = !binding.chkDurTiempoEdit.isChecked
            binding.edtCantRepEdit.isEnabled = binding.chkRepeticionEdit.isChecked
            binding.edtimeDuracionEjercicioEdit.hint = if (binding.chkRepeticionEdit.isChecked)
                getString(R.string.estimation_of_the_exercise) else
                getString(R.string.duration_of_exercise)
        }

        binding.btnAddImagen.setOnClickListener {
            if (ejercicioSeleccionado != null){
                val options = arrayOf("Imagen", "GIF", "Video", "Cancel")
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.select_the_file_type))
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                ultimoFormato = MediaTipo.IMAGE
                                selectFile(MediaTipo.IMAGE)
                            }
                            1 -> {
                                ultimoFormato = MediaTipo.GIF
                                selectFile(MediaTipo.GIF)
                            }
                            2 -> {
                                ultimoFormato = MediaTipo.VIDEO
                                selectFile(MediaTipo.VIDEO)
                            }
                            3 -> {}
                        }
                    }
                    .show()
            }else{
                Toast.makeText(requireContext(),
                    getString(R.string.you_must_click_on_the_list_clicking_on_select), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun showOptionsMenu(ejercicio: Ejercicio) {
        // Mostrar un menú con opciones de copiar, duplicar o eliminar el ejercicio
        AlertDialog.Builder(requireContext())
            .setItems(arrayOf(
                getString(R.string.select),
                getString(R.string.copy),
                getString(R.string.doublex),
                getString(R.string.eliminate),
                getString(R.string.cancel))) { _, which ->
                when (which) {
                    0 -> seleccionarEjercicio(ejercicio)
                    1 -> copyEjercicio(ejercicio)
                    2 -> duplicateEjercicio(ejercicio)
                    3 -> deleteEjercicio(ejercicio)
                    4 -> {}
                }
            }
            .show()
    }
    // Define una variable para registrar el resultado de la actividad
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                handleSelectedFile(it)
                //Log.d(TAG+" - startForResult", "Archivo seleccionado: $uri valor: $it")
            }
        }
    }

    // Función para abrir el selector de archivos
    private fun selectFile(mediaTipo: MediaTipo) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            when (mediaTipo) {
                MediaTipo.IMAGE -> type = "image/*"
                MediaTipo.GIF -> type = "image/gif"
                MediaTipo.VIDEO -> type = "video/*"
                MediaTipo.IMAGE_SEQUENCE -> {} // Aquí podrías definir el comportamiento para IMAGE_SEQUENCE si es necesario.
            }
        }
        startForResult.launch(intent)
    }

    // Función para manejar el archivo seleccionado
    private fun handleSelectedFile(uri: Uri) {
        val ejercicioId = viewModel.currentEjercicio.value?.ID ?: 0
        val currentDate = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
        val fileName = getFileNameFromUri(uri) ?: "temp_file"
        var customFileName = "foto_${fileName}_${currentDate}_${ejercicioId}"

        // Verificar si ya existe un archivo con el mismo nombre
        var num = 0
        while (fileManager.fileExistsInPrivateStorage(customFileName)) {
            num++
            customFileName = "foto${num}_${viewModel.currentEjercicio.value?.Nombre}_${currentDate}_${ejercicioId}"
        }

        val file = fileManager.copyFileToPrivateStorage(uri, customFileName)
        file?.let {
            val media = Media(
                ejercicioId = ejercicioId,
                tipo = ultimoFormato ?: MediaTipo.IMAGE,
                ruta = it.absolutePath
            )
            viewModel.insertMedia(media)
        }
    }


    private fun copyEjercicio(ejercicio: Ejercicio) {
        viewModel.cambiarEjercicioActual(null)
        ejercicioSeleccionado = null
        viewModel.loadMediaForCurrentExercise(null)
        clearFields()
        changeEnableFieldsEspecif(true)
        binding.btnAddImagen.isEnabled = false
        binding.viewPagerMedia.isEnabled = false
        llenarDatos(ejercicio)
        binding.btnEditarEjercicio.text = getString(R.string.add_exercise)
    }
    private fun seleccionarEjercicio(ejercicio: Ejercicio) {
        viewModel.cambiarEjercicioActual(ejercicio)
        viewModel.loadMediaForCurrentExercise(ejercicio.ID)
        ejercicioSeleccionado = ejercicio //variable que indica el ejercicio seleccionado, es aparte del view model current excercise
        llenarDatos(ejercicio)
        binding.btnEditarEjercicio.text = getString(R.string.edit_exercise)
    }

    private fun duplicateEjercicio(ejercicio: Ejercicio) {
        viewModel.cambiarEjercicioActual(null)
        clearFields()
        val newEjercicio = Ejercicio(
            ID = 0,
            Nombre = ejercicio.Nombre,
            DEjercicio = ejercicio.DEjercicio,
            DDescanso = ejercicio.DDescanso,
            Objetivo = ejercicio.Objetivo,
            isObjetivo = ejercicio.isObjetivo,
            rutinaId = rutinaID
        )
        viewModel.soloAdd(newEjercicio)
    }

    private fun deleteEjercicio(ejercicio: Ejercicio) {
        clearFields()
        // Verificar si es el último ejercicio en la lista
        if (viewModel.ejercicios.value?.size == 1) {
            // Mostrar un cuadro de diálogo de confirmación
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_routine))
                .setMessage(getString(R.string.this_is_the_last_exercise_in_the_routine_are_you_sure_you_want_to_delete_it_this_will_also_remove_the_routine_and_return_you_to_the_main_page))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    // Eliminar la rutina y regresar a la página principal
                    viewModel.deleteRutinaWithExercises(ejercicio.rutinaId)
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.EditEjerFragment, true)
                        .build()
                    findNavController().navigate(R.id.action_EditEjerFragment_to_ListRecyclerFragment, null, navOptions)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // Eliminar solo el ejercicio
            viewModel.deleteEjercicio(ejercicio)
            viewModel.cambiarEjercicioActual(null)
            viewModel.loadMediaForCurrentExercise(null)
        }
    }

    private fun darFormato(string: String) : String{
        val partes = string.split(":")
        val minutos = partes[0].padStart(2, '0')
        val segundos = partes[1].padStart(2, '0')
        return "${minutos}:${segundos}"
    }
    private fun addTimeTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (isValidTime(s.toString())) {
                        editText.error = null
                        negacion = false
                    } else {
                        editText.error = getString(R.string.incorrect_format_use_mm_ss)
                        negacion = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isValidTime(time: String): Boolean {
        // Expresión regular para verificar el formato MM:SS
        val regex = "^([01]?\\d|2[0-3]):([0-5]?\\d)$".toRegex()
        return time.matches(regex)
    }

    private fun addNumberTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    val number = input.toIntOrNull()
                    if (number != null && number > 0) {
                        editText.error = null
                        negacion2 = false
                    } else {
                        editText.error = getString(R.string.enter_a_number_greater_than_zero)
                        negacion2 = true
                    }
                } else {
                    editText.error = getString(R.string.the_field_cannot_be_empty)
                    negacion2 = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    // Función para validar los campos de entrada
    private fun areFieldsValid(): Boolean {
        val tipo = binding.chkRepeticionEdit.isChecked
        return binding.edtNombreEjercicioEdit.text.isNotEmpty() &&
                binding.edtimeDuracionEjercicioEdit.text.isNotEmpty() &&
                binding.edtimeDuracionDescansoEdit.text.isNotEmpty() &&
                !negacion &&
                (!tipo || (tipo && !negacion2))
    }

    // Función para limpiar los campos de entrada
    private fun clearFields() {
        binding.edtNombreEjercicioEdit.text.clear()
        binding.edtimeDuracionEjercicioEdit.text.clear()
        binding.edtimeDuracionDescansoEdit.text.clear()
        binding.edtCantRepEdit.text?.clear()
    }
    private fun changeEnableFieldsEspecif(estado : Boolean){
        binding.edtCantRepEdit.isEnabled = estado
        binding.chkRepeticionEdit.isEnabled = estado
        binding.chkDurTiempoEdit.isEnabled = estado
        binding.edtNombreEjercicioEdit.isEnabled = estado
        binding.edtimeDuracionEjercicioEdit.isEnabled = estado
        binding.edtimeDuracionDescansoEdit.isEnabled = estado
        binding.btnAddImagen.isEnabled = estado
        binding.btnEditarEjercicio.isEnabled = estado
        binding.viewPagerMedia.isEnabled = estado
    }
    private fun llenarDatos(ejercicio: Ejercicio){
        binding.edtNombreEjercicioEdit.setText(ejercicio.Nombre)
        binding.edtimeDuracionEjercicioEdit.setText(ejercicio.DEjercicio)
        binding.edtimeDuracionDescansoEdit.setText(ejercicio.DDescanso)
        binding.chkRepeticionEdit.isChecked = ejercicio.isObjetivo
        binding.chkDurTiempoEdit.isChecked = !ejercicio.isObjetivo
        binding.edtCantRepEdit.setText(if (ejercicio.isObjetivo) ejercicio.Objetivo else "")
        binding.edtCantRepEdit.isEnabled = binding.chkRepeticionEdit.isChecked
    }
    private fun checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE)
        }
    }
    companion object {
        const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                // Permiso concedido, puedes proceder a cargar los medios
                loadMedia()
            } else {
                // Permiso denegado, maneja el caso donde no se permite el acceso al almacenamiento
                Toast.makeText(requireContext(),
                    getString(R.string.you_did_not_allow_access_to_the_files), Toast.LENGTH_SHORT).show()
            }
        }
    private fun requestPermissionsIfNeeded() {
        activity?.let {
            if (hasPermissions(activity as Context, PERMISSIONS)) {
                loadMedia()
            } else {
                permReqLauncher.launch(PERMISSIONS)
            }
        }
    }

    // Método para verificar si los permisos ya están concedidos
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Método para cargar los medios
    private fun loadMedia() {
        // Implementa la lógica para cargar los medios aquí
    }
    private var mediaReplace : Media? = null
    private fun showMediaOptionsMenu(media: Media) {
        AlertDialog.Builder(requireContext())
            .setItems(arrayOf("Reemplazar imagen", "Borrar imagen", "Borrar todas las imágenes", "Cancelar")) { _, which ->
                when (which) {
                    0 -> {
                        mediaReplace = media
                        replaceImage()
                    }
                    1 -> deleteImage(media)
                    2 -> deleteAllImages()
                    3 -> { /* Cancelar */ }
                }
            }
            .show()
    }

    private fun replaceImage() {
        if (ejercicioSeleccionado != null){
            val options = arrayOf("Imagen", "GIF", "Video", "Cancel")
            AlertDialog.Builder(requireContext())
                .setTitle("Selecciona el tipo de archivo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            ultimoFormato = MediaTipo.IMAGE
                            selectReplace(MediaTipo.IMAGE)
                        }
                        1 -> {
                            ultimoFormato = MediaTipo.GIF
                            selectReplace(MediaTipo.GIF)
                        }
                        2 -> {
                            ultimoFormato = MediaTipo.VIDEO
                            selectReplace(MediaTipo.VIDEO)
                        }
                        3 -> {}
                    }
                }
                .show()
        }else{
            Toast.makeText(requireContext(), "Debes pulsar en la lista, pulsando en seleccionar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImage(media: Media) {
        // Implementa la lógica para borrar la imagen
        viewModel.deleteMedia(media)
    }

    private fun deleteAllImages() {
        // Implementa la lógica para borrar todas las imágenes
        viewModel.deleteMediaWithEjercicioId()
    }
    // Función para abrir el selector de archivos
    private fun selectReplace(mediaTipo: MediaTipo) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            when (mediaTipo) {
                MediaTipo.IMAGE -> type = "image/*"
                MediaTipo.GIF -> type = "image/gif"
                MediaTipo.VIDEO -> { type = "video/*" }
                MediaTipo.IMAGE_SEQUENCE ->{ }
            }
        }
        startForResultMedia.launch(intent)
    }
    private val startForResultMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                handleSelectedRemplace(it)
            }
        }
    }
    // Función para manejar el archivo seleccionado
    private fun handleSelectedRemplace(uri: Uri) {
        val ejercicioId = viewModel.currentEjercicio.value?.ID ?: 0
        val currentDate = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())
        val fileName = getFileNameFromUri(uri) ?: "temp_file"
        var customFileName = "foto_${fileName}_${currentDate}_${ejercicioId}"

        // Verificar si ya existe un archivo con el mismo nombre
        var num = 0
        while (fileManager.fileExistsInPrivateStorage(customFileName)) {
            num++
            customFileName = "foto${num}x${num+1}_${fileName}_${currentDate}_${ejercicioId}"
        }
        val file = fileManager.copyFileToPrivateStorage(uri,customFileName)

        file?.let {
            if (mediaReplace != null){
                val media = Media(
                    id = mediaReplace!!.id,
                    ejercicioId = mediaReplace!!.ejercicioId,
                    tipo = ultimoFormato ?: MediaTipo.IMAGE,
                    ruta = it.absolutePath
                )
                //Log.d(TAG, "nombre: ${it.absolutePath } Media: $media")
                viewModel.updateMedia(media)
            }
        }
    }
    // Función para obtener el nombre de archivo desde la URI
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = if (columnIndex != -1) {
                    val displayName = it.getString(columnIndex)
                    // Remover la extensión del nombre del archivo
                    val nameWithoutExtension = displayName.substringBeforeLast(".")
                    nameWithoutExtension
                } else {
                    // Si no se encuentra DISPLAY_NAME, generar un nombre genérico
                    "archivo_${System.currentTimeMillis()}"
                }
            }
        }
        return fileName
    }
}