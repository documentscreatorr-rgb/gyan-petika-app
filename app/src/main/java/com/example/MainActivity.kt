package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.StudyPost
import com.example.ui.MainViewModel
import com.example.ui.screens.ArticleReaderScreen
import com.example.ui.screens.CategoryPostsScreen
import com.example.ui.screens.ClassHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.theme.GyanPetikaTheme
import com.example.ui.theme.PrimaryNavy
import com.example.util.configureAppCheck

private object Routes {
  const val HOME = "home"
  const val SEARCH = "search"
  const val SAVED = "saved"
  const val CLASS_HUB = "class_hub/{className}"
  const val NOTICES = "notices"
  const val SUBJECT_DETAIL = "subject_detail/{className}/{subjectName}"
  const val CATEGORY_POSTS = "category_posts/{className}/{subjectName}/{categoryTitle}"
  const val READER = "reader/{postId}"

  fun classHub(className: String) = "class_hub/${Uri.encode(className)}"

  fun subjectDetail(className: String, subjectName: String) =
    "subject_detail/${Uri.encode(className)}/${Uri.encode(subjectName)}"

  fun categoryPosts(className: String, subjectName: String, categoryTitle: String) =
    "category_posts/${Uri.encode(className)}/${Uri.encode(subjectName)}/${Uri.encode(categoryTitle)}"

  fun reader(postId: String) = "reader/${Uri.encode(postId)}"
}

private data class BottomNavTab(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val bottomTabs =
  listOf(
    BottomNavTab(
      route = Routes.HOME,
      labelRes = R.string.nav_home,
      selectedIcon = Icons.Rounded.Home,
      unselectedIcon = Icons.Outlined.Home,
      testTag = "tab_home",
    ),
    BottomNavTab(
      route = Routes.SEARCH,
      labelRes = R.string.nav_search,
      selectedIcon = Icons.Rounded.Search,
      unselectedIcon = Icons.Outlined.Search,
      testTag = "tab_search",
    ),
    BottomNavTab(
      route = Routes.SAVED,
      labelRes = R.string.nav_saved,
      selectedIcon = Icons.Rounded.Bookmark,
      unselectedIcon = Icons.Outlined.BookmarkBorder,
      testTag = "tab_saved",
    ),
  )

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    configureAppCheck(this, intent)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      GyanPetikaTheme(
        themeMode = uiState.preferences.themeMode,
        eyeComfortPaperMode = uiState.preferences.eyeComfortPaperMode,
      ) {
        GyanPetikaApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
private fun GyanPetikaApp(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val translateUiState by viewModel.translateUiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val snackbarHostState = remember { SnackbarHostState() }
  var showSettingsSheet by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(uiState.statusBannerMessage) {
    val msg = uiState.statusBannerMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.dismissStatusMessage()
    }
  }

  val showMainNav =
    currentRoute == Routes.HOME || currentRoute == Routes.SEARCH || currentRoute == Routes.SAVED

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  fun openPost(post: StudyPost) {
    navController.navigate(Routes.reader(post.id))
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (showMainNav && !useNavigationRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
          ) {
            bottomTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                  if (tab.route == Routes.SAVED && uiState.savedPosts.isNotEmpty()) {
                    BadgedBox(
                      badge = {
                        Badge { Text("${uiState.savedPosts.size}") }
                      }
                    ) {
                      Icon(imageVector = icon, contentDescription = stringResource(tab.labelRes))
                    }
                  } else {
                    Icon(imageVector = icon, contentDescription = stringResource(tab.labelRes))
                  }
                },
                label = { Text(text = stringResource(tab.labelRes)) },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryNavy,
                    selectedTextColor = PrimaryNavy,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  ),
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
      ) {
        if (showMainNav && useNavigationRail) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(),
          ) {
            bottomTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                  Icon(imageVector = icon, contentDescription = stringResource(tab.labelRes))
                },
                label = { Text(text = stringResource(tab.labelRes)) },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = PrimaryNavy,
                    selectedTextColor = PrimaryNavy,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  ),
                modifier = Modifier.testTag("${tab.testTag}_rail"),
              )
            }
          }
        }

        Box(
          modifier = Modifier.weight(1f).fillMaxHeight(),
          contentAlignment = Alignment.TopCenter,
        ) {
          NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize(),
          ) {
            composable(Routes.HOME) {
              HomeScreen(
                uiState = uiState,
                onSettingsClick = { showSettingsSheet = true },
                onSearchClick = { navigateToTab(Routes.SEARCH) },
                onSelectClassPill = viewModel::selectClassFilter,
                onOpenClassHub = { className ->
                  navController.navigate(Routes.classHub(className))
                },
                onViewAllNoticesClick = {
                  navController.navigate(Routes.NOTICES)
                },
                onOpenPost = ::openPost,
                onToggleBookmark = viewModel::toggleBookmark,
                onSyncFeed = { viewModel.syncFeed(silent = false) },
              )
            }

            composable(Routes.NOTICES) {
              NoticesScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onOpenPost = ::openPost,
                onToggleBookmark = viewModel::toggleBookmark,
                onAutoSync = { viewModel.syncFeed(silent = true) },
              )
            }

            composable(Routes.SEARCH) {
              SearchScreen(
                uiState = uiState,
                onQueryChange = viewModel::updateSearchQuery,
                onClassFilterChange = viewModel::updateSearchClassFilter,
                onSettingsClick = { showSettingsSheet = true },
                onOpenPost = ::openPost,
                onToggleBookmark = viewModel::toggleBookmark,
              )
            }

            composable(Routes.SAVED) {
              SavedScreen(
                uiState = uiState,
                onSettingsClick = { showSettingsSheet = true },
                onOpenPost = ::openPost,
                onToggleBookmark = viewModel::toggleBookmark,
                onBrowseHomeClick = { navigateToTab(Routes.HOME) },
              )
            }

            composable(
              route = Routes.CLASS_HUB,
              arguments = listOf(navArgument("className") { type = NavType.StringType }),
            ) { entry ->
              val className = Uri.decode(entry.arguments?.getString("className") ?: "Class 10")
              ClassHubScreen(
                className = className,
                onBackClick = { navController.popBackStack() },
                onSelectSubject = { subject ->
                  navController.navigate(Routes.subjectDetail(className, subject.name))
                },
              )
            }

            composable(
              route = Routes.SUBJECT_DETAIL,
              arguments =
                listOf(
                  navArgument("className") { type = NavType.StringType },
                  navArgument("subjectName") { type = NavType.StringType },
                ),
            ) { entry ->
              val className = Uri.decode(entry.arguments?.getString("className") ?: "Class 10")
              val subjectName =
                Uri.decode(entry.arguments?.getString("subjectName") ?: "Information Technology")
              val subjectPosts =
                remember(uiState.posts, className, subjectName) {
                  viewModel.postsForSubject(className, subjectName, null)
                }

              SubjectDetailScreen(
                className = className,
                subjectName = subjectName,
                allSubjectPosts = subjectPosts,
                onBackClick = { navController.popBackStack() },
                onOpenCategory = { categoryTitle ->
                  navController.navigate(Routes.categoryPosts(className, subjectName, categoryTitle))
                },
              )
            }

            composable(
              route = Routes.CATEGORY_POSTS,
              arguments =
                listOf(
                  navArgument("className") { type = NavType.StringType },
                  navArgument("subjectName") { type = NavType.StringType },
                  navArgument("categoryTitle") { type = NavType.StringType },
                ),
            ) { entry ->
              val className = Uri.decode(entry.arguments?.getString("className") ?: "Class 10")
              val subjectName =
                Uri.decode(entry.arguments?.getString("subjectName") ?: "Information Technology")
              val categoryTitle =
                Uri.decode(entry.arguments?.getString("categoryTitle") ?: "All Posts")

              val allSubjectPosts =
                remember(uiState.posts, className, subjectName) {
                  viewModel.postsForSubject(className, subjectName, null)
                }
              val filteredPosts =
                remember(uiState.posts, className, subjectName, categoryTitle) {
                  viewModel.postsForSubject(className, subjectName, categoryTitle)
                }

              CategoryPostsScreen(
                className = className,
                subjectName = subjectName,
                categoryTitle = categoryTitle,
                filteredPosts = filteredPosts,
                allSubjectPosts = allSubjectPosts,
                savedPostIds = uiState.preferences.savedPostIds,
                density = uiState.preferences.listDensity,
                onBackClick = { navController.popBackStack() },
                onOpenPost = ::openPost,
                onToggleBookmark = viewModel::toggleBookmark,
                onShowAllSubjectPosts = {
                  navController.navigate(Routes.categoryPosts(className, subjectName, "All Posts")) {
                    popUpTo(Routes.subjectDetail(className, subjectName)) { inclusive = false }
                  }
                },
              )
            }

            composable(
              route = Routes.READER,
              arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) { entry ->
              val postId = Uri.decode(entry.arguments?.getString("postId").orEmpty())
              val post =
                remember(uiState.posts, postId) {
                  viewModel.findPostById(postId) ?: uiState.posts.first()
                }
              val context = LocalContext.current
              ArticleReaderScreen(
                post = post,
                preferences = uiState.preferences,
                translateUiState = translateUiState,
                onBackClick = { navController.popBackStack() },
                onFontDecrease = { viewModel.stepReaderFontSize(-2) },
                onFontIncrease = { viewModel.stepReaderFontSize(2) },
                onTogglePaperMode = {
                  viewModel.setEyeComfortPaperMode(!uiState.preferences.eyeComfortPaperMode)
                },
                onToggleBookmark = { viewModel.toggleBookmark(post.id) },
                onLabelClick = { label ->
                  viewModel.searchByLabel(label)
                  navigateToTab(Routes.SEARCH)
                },
                onInternalPostLinkClick = { url ->
                  val target = viewModel.findPostByUrl(url)
                  if (target != null) {
                    openPost(target)
                  } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                  }
                },
                onTranslatePost = { modeKey, language, title, segments, onTranslated ->
                  viewModel.translatePost(
                    postId = post.id,
                    modeKey = modeKey,
                    targetLanguage = language,
                    title = title,
                    segments = segments,
                    onTranslated = onTranslated,
                  )
                },
                onResetTranslation = viewModel::resetTranslation,
              )
            }
          }
        }
      }
    }

    if (showSettingsSheet) {
      SettingsBottomSheet(
        preferences = uiState.preferences,
        syncStatus = uiState.syncStatus,
        onDismiss = { showSettingsSheet = false },
        onThemeChange = viewModel::setThemeMode,
        onDensityChange = viewModel::setListDensity,
        onTextSizeChange = viewModel::setTextSizePreset,
        onPaperModeChange = viewModel::setEyeComfortPaperMode,
        onAutoCacheChange = viewModel::setAutoCacheOffline,
        onSyncFeed = { viewModel.syncFeed(silent = false) },
        onRebuildCache = viewModel::rebuildCache,
        onResetDefaults = viewModel::resetDefaults,
      )
    }
  }
}
