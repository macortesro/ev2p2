@file:OptIn(ExperimentalMaterial3Api::class)

package cl.mcortesr.android.ev2p2

import android.bluetooth.le.AdvertiseSettings
import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.mcortesr.android.ev2p2.data.modelo.Producto
import cl.mcortesr.android.ev2p2.ui.theme.vm.ConfiguracionViewModel
import cl.mcortesr.android.ev2p2.ui.theme.vm.ProductosViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val productosVm: ProductosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("MainActivity::onCreate", "Recuperando productos en Disco" )
        try {
            productosVm.obtenerProductosGuardadosEnDisco((openFileInput(ProductosViewModel.FILE_NAME)))
        } catch (e:Exception) {
            Log.v("MainActivity::onCreate", "Archivo con productos no existe!!")
        }

        setContent {
            AppCompras()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.v("MainActivity::onPause", "Guardando a disco")
        productosVm.guardarProductosEnDisco(openFileOutput(ProductosViewModel.FILE_NAME, MODE_PRIVATE))
    }

    override fun onStop() {
        super.onStop()
        Log.v("MainActivity::onStop", "Guardando a disco")
    }
}

@Composable
fun AppCompras(
    navController: NavHostController = rememberNavController())
    {
    NavHost(navController = navController,
        startDestination = "home")
        {
        composable("home"){
            HomePageUI(
                onButtonSettingsClicked = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings"){
            SettingsPageUI(
                onBackButtonClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppComprasTopBar(
    title:String = "",
    showSettingsButton:Boolean = true,
    onButtonSettingsClicked: () -> Unit = {},
    showBackButton:Boolean = false,
    onBackButtonClicked:() -> Unit = {}
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    onBackButtonClicked()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás"
                    )
                }
            }
        },
        actions = {
            if (showSettingsButton) {
                IconButton(onClick = {
                    onButtonSettingsClicked()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Configuración"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Preview(showSystemUi = true)
@Composable
fun SettingsPageUI(
    onBackButtonClicked: () -> Unit = {},
    configuracionViewModel: ConfiguracionViewModel = viewModel(),
    productosViewModel: ProductosViewModel = viewModel()
) {
    val context = LocalContext.current
    val textConfiguration = context.getString(R.string.pag_configuracion)

    var seDebeOrdenarAlfabeticamente by remember { mutableStateOf(false) }
    var seDebeMostrarItemsPorComprar by remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            AppComprasTopBar(
                title = textConfiguration,
                showSettingsButton = false,
                showBackButton = true,
                onBackButtonClicked = onBackButtonClicked
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 10.dp,
                    vertical = it.calculateTopPadding()
                )
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Switch(
                    checked = seDebeOrdenarAlfabeticamente,
                    onCheckedChange = {
                        seDebeOrdenarAlfabeticamente = it
                        productosViewModel.activarOrdenAlfabetico()
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Switch(
                    checked = seDebeMostrarItemsPorComprar,
                    onCheckedChange = {
                        seDebeMostrarItemsPorComprar = it
                        productosViewModel.activarMostrarPrimeroNoEncontrados()
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomePageUI(
    productosVm: ProductosViewModel = viewModel(),
    onButtonSettingsClicked:() -> Unit = {},
    configuracionViewModel: ConfiguracionViewModel = viewModel()
    ) {
    val context = LocalContext.current
    val textoLogo = context.getString(R.string.logo)
    val textTituloApp = context.getString(R.string.app_name)
    val uiState by productosVm.uiState.collectAsStateWithLifecycle()
    var mostrarMensaje by rememberSaveable {
        mutableStateOf(false)
    }
    var primeraEjecucion by rememberSaveable {
        mutableStateOf(true)
    }

    LaunchedEffect(uiState.mensaje) {
        if(!primeraEjecucion){
            mostrarMensaje = true
            delay(2_000)
            mostrarMensaje = false
        }
        primeraEjecucion = false
    }

    Scaffold (
        topBar = {
            AppComprasTopBar(
                title = textTituloApp,
                onButtonSettingsClicked = onButtonSettingsClicked
            )
        }
    ) { paddingValues ->
        Spacer(modifier = Modifier.height(15.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = mostrarMensaje,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

            }

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = textoLogo,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .size(70.dp)
            )
            ProductoFormUI {
                productosVm.agregarProducto(it)
            }
            Spacer(modifier = Modifier.height(5.dp))
            ProductoListaUI(
                productos = uiState.productos,
                onDelete = {
                    productosVm.eliminarProductos(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormUI(
    onClickAgregarProducto: (producto:String) -> Unit
) {
    val contexto = LocalContext.current
    val textTaskPlaceholder = contexto.getString(R.string.producto_form_ejemplo)
    val textButtonAddTask = contexto.getString(R.string.producto_fotm_agregar)

    val(nombreProducto, setNombreProducto) = rememberSaveable {
        mutableStateOf("")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ){

        }

    }


@Composable
fun ProductoListaUI(
    productos:List<Producto>,
    onDelete: (p:Producto) -> Unit
) {
    LazyColumn() {
        items(productos) {
            ProductoListaItemUI(it, onDelete)
        }
    }
}

@Composable
fun ProductoListaItemUI(
    producto: Producto,
    onDelete: (p: Producto) -> Unit,
    productosViewModel: ProductosViewModel = viewModel()
) {
    var productoEncontrado by rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val textoEliminarTarea = context.getString(R.string.producto_form_eliminar)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Checkbox(
                checked = productoEncontrado,
                onCheckedChange = {
                    productoEncontrado = it
                    productosViewModel.marcarProductoEncontrado(producto.id, it)
                },
                modifier = Modifier.padding(4.dp)
            )
            IconButton(onClick = {
                Log.v("TareaListaItemUI::TextButton", "onClick DELETE")
                onDelete(producto)
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = textoEliminarTarea,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        HorizontalDivider()
    }
}





