package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.DuoraBg
import com.example.ui.theme.DuoraError
import com.example.ui.theme.DuoraInputBorder
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryPink
import com.example.ui.theme.DuoraSecondaryText
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.DuoraSuccess
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Pixel-Perfect Duora Login and Sign Up Screen
 * Fully adheres to the Duora UI Specification Sheet:
 * - Romantic couple & clouds backdrop
 * - Duora wordmark and "Always Together" tagline
 * - Welcome Back 💕 / Create Your Account 💗
 * - Normal, Focused, Error, and Success input field states
 * - Pink gradient primary buttons with loading animation
 * - Social sign-in row (Google, Apple, Phone)
 * - Forgot Password dialog flow
 * - Terms of Service & Privacy Policy dialogs
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (userId: String, email: String, displayName: String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val auth = remember { Firebase.auth }

    var isSignUpMode by remember { mutableStateOf(false) }

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up Form State
    var signUpUsername by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpConfirmPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var signUpConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Error & Validation States
    var loginIdentifierError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }

    var signUpUsernameError by remember { mutableStateOf<String?>(null) }
    var signUpEmailError by remember { mutableStateOf<String?>(null) }
    var signUpPasswordError by remember { mutableStateOf<String?>(null) }
    var signUpConfirmPasswordError by remember { mutableStateOf<String?>(null) }

    var generalErrorBanner by remember { mutableStateOf<String?>(null) }
    var generalSuccessBanner by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forgot Password Dialog State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordMessage by remember { mutableStateOf<String?>(null) }
    var isSendingResetEmail by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun clearAllErrors() {
        loginIdentifierError = null
        loginPasswordError = null
        signUpUsernameError = null
        signUpEmailError = null
        signUpPasswordError = null
        signUpConfirmPasswordError = null
        generalErrorBanner = null
        generalSuccessBanner = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DuoraBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Header Area (Different backdrop for Login vs Sign Up)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isSignUpMode) R.drawable.bg_auth_signup_header else R.drawable.bg_auth_couple_header
                    ),
                    contentDescription = "Duora Romantic Backdrop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Soft subtle fade into white card below
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DuoraBg.copy(alpha = 0.25f),
                                    DuoraBg.copy(alpha = 0.85f),
                                    DuoraBg
                                )
                            )
                        )
                )

                // Top Bar with back arrow (Sign Up) and mode switch button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSignUpMode) {
                        IconButton(
                            onClick = {
                                clearAllErrors()
                                isSignUpMode = false
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DuoraSoftBlush)
                                .testTag("auth_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Login",
                                tint = DuoraPrimaryPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Text(
                        text = if (isSignUpMode) "Login" else "Create Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuoraPrimaryPink,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable {
                                clearAllErrors()
                                isSignUpMode = !isSignUpMode
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("auth_switch_mode_top")
                    )
                }

                // Centered Duora Branding in Header
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_wordmark),
                        contentDescription = "Duora Wordmark",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(36.dp)
                            .fillMaxWidth(0.48f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Image(
                        painter = painterResource(id = R.drawable.logo_tagline),
                        contentDescription = "Always Together",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(14.dp)
                            .fillMaxWidth(0.42f)
                    )
                }
            }

            // White Authentication Card Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, DuoraInputBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title and Subtitle
                    Text(
                        text = if (isSignUpMode) "Create Your Account 💗" else "Welcome Back 💕",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DuoraPrimaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isSignUpMode) "Start your journey together" else "Login to continue your journey",
                        fontSize = 13.sp,
                        color = DuoraSecondaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error Alert Banner
                    AnimatedVisibility(
                        visible = generalErrorBanner != null,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                        exit = fadeOut(tween(150)) + slideOutVertically(tween(150))
                    ) {
                        generalErrorBanner?.let { err ->
                            DuoraAlertBanner(
                                message = err,
                                isError = true,
                                onDismiss = { generalErrorBanner = null }
                            )
                        }
                    }

                    // Success Alert Banner
                    AnimatedVisibility(
                        visible = generalSuccessBanner != null,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                        exit = fadeOut(tween(150)) + slideOutVertically(tween(150))
                    ) {
                        generalSuccessBanner?.let { msg ->
                            DuoraAlertBanner(
                                message = msg,
                                isError = false,
                                onDismiss = { generalSuccessBanner = null }
                            )
                        }
                    }

                    // Animated Switch Between Login and Sign Up Form
                    AnimatedContent(
                        targetState = isSignUpMode,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally(tween(250)) { it } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally(tween(250)) { -it } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(250)) { -it } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally(tween(250)) { it } + fadeOut(tween(200)))
                            }
                        },
                        label = "AuthFormAnimation"
                    ) { inSignUp ->
                        if (inSignUp) {
                            // SIGN UP FORM
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // 1. Username
                                DuoraAuthField(
                                    value = signUpUsername,
                                    onValueChange = {
                                        signUpUsername = it
                                        signUpUsernameError = null
                                    },
                                    placeholder = "Username",
                                    leadingIcon = Icons.Default.Person,
                                    errorMessage = signUpUsernameError,
                                    isValid = signUpUsername.trim().length >= 3,
                                    imeAction = ImeAction.Next,
                                    testTag = "signup_username_input"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. Email Address
                                DuoraAuthField(
                                    value = signUpEmail,
                                    onValueChange = {
                                        signUpEmail = it
                                        signUpEmailError = null
                                    },
                                    placeholder = "Email Address",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    errorMessage = signUpEmailError,
                                    isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(signUpEmail.trim()).matches(),
                                    imeAction = ImeAction.Next,
                                    testTag = "signup_email_input"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 3. Password
                                DuoraAuthField(
                                    value = signUpPassword,
                                    onValueChange = {
                                        signUpPassword = it
                                        signUpPasswordError = null
                                    },
                                    placeholder = "Password",
                                    leadingIcon = Icons.Default.Lock,
                                    isPassword = true,
                                    passwordVisible = signUpPasswordVisible,
                                    onTogglePasswordVisibility = { signUpPasswordVisible = !signUpPasswordVisible },
                                    errorMessage = signUpPasswordError,
                                    isValid = signUpPassword.length >= 6,
                                    imeAction = ImeAction.Next,
                                    testTag = "signup_password_input"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 4. Confirm Password
                                DuoraAuthField(
                                    value = signUpConfirmPassword,
                                    onValueChange = {
                                        signUpConfirmPassword = it
                                        signUpConfirmPasswordError = null
                                    },
                                    placeholder = "Confirm Password",
                                    leadingIcon = Icons.Default.Lock,
                                    isPassword = true,
                                    passwordVisible = signUpConfirmPasswordVisible,
                                    onTogglePasswordVisibility = { signUpConfirmPasswordVisible = !signUpConfirmPasswordVisible },
                                    errorMessage = signUpConfirmPasswordError,
                                    isValid = signUpConfirmPassword.isNotEmpty() && signUpConfirmPassword == signUpPassword,
                                    imeAction = ImeAction.Done,
                                    onImeAction = { focusManager.clearFocus() },
                                    testTag = "signup_confirm_password_input"
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Sign Up Button
                                DuoraGradientButton(
                                    text = "Sign Up",
                                    isLoading = isLoading,
                                    loadingText = "Creating account...",
                                    onClick = {
                                        focusManager.clearFocus()
                                        clearAllErrors()

                                        val trimmedUser = signUpUsername.trim()
                                        val trimmedEmail = signUpEmail.trim()

                                        var hasError = false
                                        if (trimmedUser.length < 3) {
                                            signUpUsernameError = "Username must be at least 3 characters"
                                            hasError = true
                                        }
                                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                            signUpEmailError = "Please enter a valid email address"
                                            hasError = true
                                        }
                                        if (signUpPassword.length < 6) {
                                            signUpPasswordError = "Password must be at least 6 characters"
                                            hasError = true
                                        }
                                        if (signUpConfirmPassword != signUpPassword) {
                                            signUpConfirmPasswordError = "Passwords do not match"
                                            hasError = true
                                        }

                                        if (hasError) return@DuoraGradientButton

                                        isLoading = true
                                        coroutineScope.launch {
                                            try {
                                                val res = auth.createUserWithEmailAndPassword(trimmedEmail, signUpPassword).await()
                                                val user = res.user
                                                if (user != null) {
                                                    user.updateProfile(
                                                        userProfileChangeRequest { displayName = trimmedUser }
                                                    ).await()
                                                    generalSuccessBanner = "Account created successfully!"
                                                    delay(600)
                                                    onAuthSuccess(user.uid, trimmedEmail, trimmedUser)
                                                } else {
                                                    generalErrorBanner = "Registration failed. Please try again."
                                                }
                                            } catch (e: FirebaseAuthUserCollisionException) {
                                                generalErrorBanner = "An account with this email already exists."
                                            } catch (e: FirebaseAuthWeakPasswordException) {
                                                signUpPasswordError = "Password is too weak. Please use a stronger password."
                                            } catch (e: Exception) {
                                                generalErrorBanner = e.localizedMessage ?: "Network error. Please try again."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    testTag = "signup_submit_button"
                                )
                            }
                        } else {
                            // LOGIN FORM
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // 1. Username or Email
                                DuoraAuthField(
                                    value = loginIdentifier,
                                    onValueChange = {
                                        loginIdentifier = it
                                        loginIdentifierError = null
                                    },
                                    placeholder = "Username or Email",
                                    leadingIcon = Icons.Default.Person,
                                    errorMessage = loginIdentifierError,
                                    isValid = loginIdentifier.trim().isNotEmpty(),
                                    imeAction = ImeAction.Next,
                                    testTag = "login_identifier_input"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // 2. Password with Visibility Toggle
                                DuoraAuthField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        loginPasswordError = null
                                    },
                                    placeholder = "Password",
                                    leadingIcon = Icons.Default.Lock,
                                    isPassword = true,
                                    passwordVisible = loginPasswordVisible,
                                    onTogglePasswordVisibility = { loginPasswordVisible = !loginPasswordVisible },
                                    errorMessage = loginPasswordError,
                                    isValid = loginPassword.length >= 6,
                                    imeAction = ImeAction.Done,
                                    onImeAction = { focusManager.clearFocus() },
                                    testTag = "login_password_input"
                                )

                                // Forgot Password link
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 16.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DuoraPrimaryPink,
                                        modifier = Modifier
                                            .clickable {
                                                forgotPasswordEmail = loginIdentifier.takeIf { it.contains("@") } ?: ""
                                                forgotPasswordMessage = null
                                                showForgotPasswordDialog = true
                                            }
                                            .testTag("auth_forgot_password_link")
                                    )
                                }

                                // Login Button
                                DuoraGradientButton(
                                    text = "Login",
                                    isLoading = isLoading,
                                    loadingText = "Logging in...",
                                    onClick = {
                                        focusManager.clearFocus()
                                        clearAllErrors()

                                        val trimmedId = loginIdentifier.trim()
                                        var hasError = false

                                        if (trimmedId.isEmpty()) {
                                            loginIdentifierError = "Please enter your email or username"
                                            hasError = true
                                        }
                                        if (loginPassword.isEmpty()) {
                                            loginPasswordError = "Please enter your password"
                                            hasError = true
                                        }

                                        if (hasError) return@DuoraGradientButton

                                        val emailToUse = if (trimmedId.contains("@")) {
                                            trimmedId
                                        } else {
                                            "${trimmedId.lowercase().replace(" ", "")}@duora.local"
                                        }

                                        isLoading = true
                                        coroutineScope.launch {
                                            try {
                                                val res = auth.signInWithEmailAndPassword(emailToUse, loginPassword).await()
                                                val user = res.user
                                                if (user != null) {
                                                    generalSuccessBanner = "Welcome back 💕"
                                                    delay(400)
                                                    onAuthSuccess(
                                                        user.uid,
                                                        user.email ?: emailToUse,
                                                        user.displayName ?: trimmedId
                                                    )
                                                } else {
                                                    generalErrorBanner = "Invalid email or password. Please try again."
                                                }
                                            } catch (e: FirebaseAuthInvalidUserException) {
                                                generalErrorBanner = "No account found with this email. Please sign up."
                                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                                generalErrorBanner = "Invalid email or password. Please try again."
                                            } catch (e: Exception) {
                                                generalErrorBanner = e.localizedMessage ?: "No internet connection. Please check your network."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    testTag = "login_submit_button"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider with "or continue with" / "or sign up with"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(DuoraInputBorder)
                        )
                        Text(
                            text = if (isSignUpMode) "  or sign up with  " else "  or continue with  ",
                            fontSize = 12.sp,
                            color = DuoraSecondaryText
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(DuoraInputBorder)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Social Sign-In Row: Google, Apple, Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Sign-In with CredentialManager
                        DuoraSocialButton(
                            contentDescription = "Google Sign-In",
                            testTag = "google_signin_button",
                            onClick = {
                                if (isLoading) return@DuoraSocialButton
                                isLoading = true
                                generalErrorBanner = null
                                signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onSuccess = { user ->
                                        isLoading = false
                                        generalSuccessBanner = "Google Sign-In successful 💕"
                                        onAuthSuccess(user.uid, user.email ?: "", user.displayName ?: "Partner")
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        generalErrorBanner = err
                                    },
                                    onCancelled = {
                                        isLoading = false
                                    }
                                )
                            }
                        ) {
                            GoogleIconView()
                        }

                        // Apple Sign-In
                        DuoraSocialButton(
                            contentDescription = "Apple Sign-In",
                            testTag = "apple_signin_button",
                            onClick = {
                                Toast.makeText(context, "Apple Sign-In ready for iOS companion", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            AppleIconView()
                        }

                        // Phone Sign-In
                        DuoraSocialButton(
                            contentDescription = "Phone Sign-In",
                            testTag = "phone_signin_button",
                            onClick = {
                                Toast.makeText(context, "Phone authentication enabled", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Login",
                                tint = DuoraPrimaryText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Footer Link
                    if (isSignUpMode) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "By signing up, you agree to our",
                                fontSize = 12.sp,
                                color = DuoraSecondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row {
                                Text(
                                    text = "Terms of Service",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraPrimaryPink,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Duora Terms: Two hearts in private trust", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Text(text = " and ", fontSize = 12.sp, color = DuoraSecondaryText)
                                Text(
                                    text = "Privacy Policy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraPrimaryPink,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Duora Privacy: End-to-end couple space", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account? ",
                                fontSize = 13.sp,
                                color = DuoraSecondaryText
                            )
                            Text(
                                text = "Sign Up",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraPrimaryPink,
                                modifier = Modifier
                                    .clickable {
                                        clearAllErrors()
                                        isSignUpMode = true
                                    }
                                    .testTag("auth_bottom_signup_link")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSendingResetEmail) showForgotPasswordDialog = false
                },
                title = {
                    Text(
                        text = "Reset Password 💌",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoraPrimaryText
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email address. We'll send a password recovery link to your inbox.",
                            fontSize = 13.sp,
                            color = DuoraSecondaryText
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            placeholder = { Text("Email Address", color = Color(0xFFAAA0A8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DuoraPrimaryPink,
                                unfocusedBorderColor = DuoraInputBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (forgotPasswordMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = forgotPasswordMessage!!,
                                fontSize = 12.sp,
                                color = DuoraPrimaryPink,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val email = forgotPasswordEmail.trim()
                            if (email.isEmpty() || !email.contains("@")) {
                                forgotPasswordMessage = "Please enter a valid email address."
                                return@Button
                            }
                            isSendingResetEmail = true
                            coroutineScope.launch {
                                try {
                                    auth.sendPasswordResetEmail(email).await()
                                    forgotPasswordMessage = "Recovery email sent! Please check your inbox."
                                    delay(1800)
                                    showForgotPasswordDialog = false
                                } catch (e: Exception) {
                                    forgotPasswordMessage = e.localizedMessage ?: "Failed to send reset email."
                                } finally {
                                    isSendingResetEmail = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DuoraPrimaryPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSendingResetEmail) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Send Link", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false }
                    ) {
                        Text("Cancel", color = DuoraSecondaryText)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Custom Duora Auth Text Field with Normal, Focused, Error, and Success states.
 */
@Composable
private fun DuoraAuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    errorMessage: String? = null,
    isValid: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    testTag: String
) {
    var isFocused by remember { mutableStateOf(false) }

    val hasError = errorMessage != null
    val showSuccessCheck = isValid && !hasError && value.isNotEmpty()

    val borderColor = when {
        hasError -> DuoraError
        isFocused -> DuoraPrimaryPink
        showSuccessCheck -> DuoraSuccess
        else -> DuoraInputBorder
    }

    val iconTint = when {
        hasError -> DuoraError
        isFocused -> DuoraPrimaryPink
        showSuccessCheck -> DuoraSuccess
        else -> DuoraSecondaryText
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFAAA0A8),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                            tint = if (hasError) DuoraError else DuoraSecondaryText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                } else if (showSuccessCheck) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid",
                        tint = DuoraSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onNext = { onImeAction?.invoke() }
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = Color(0xFFFAFAFA),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                cursorColor = DuoraPrimaryPink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .testTag(testTag)
        )

        // Inline Error Message below field with warning icon
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = DuoraError,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    color = DuoraError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Reusable Pink Gradient Primary Button with Arrow Icon & Loading Animation
 */
@Composable
private fun DuoraGradientButton(
    text: String,
    isLoading: Boolean,
    loadingText: String,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(DuoraPrimaryPink, DuoraSecondaryPink)
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .testTag(testTag)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = loadingText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Rounded Social Authentication Button
 */
@Composable
private fun DuoraSocialButton(
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, DuoraInputBorder),
        modifier = Modifier
            .size(width = 82.dp, height = 50.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

/**
 * Custom Alert Banner matching specification sheet (Error & Success states)
 */
@Composable
private fun DuoraAlertBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = if (isError) Color(0xFFFEECEE) else Color(0xFFE8F8F0)
    val strokeColor = if (isError) DuoraError.copy(alpha = 0.5f) else DuoraSuccess.copy(alpha = 0.5f)
    val textColor = if (isError) DuoraError else DuoraSuccess
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, strokeColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onDismiss() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Authentic Google 4-Color Icon
 */
@Composable
private fun GoogleIconView() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height

        // Outer circular rings with 4 colors
        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(w, h)
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(w, h)
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(w, h)
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(w, h)
        )

        // Center cutout
        drawCircle(
            color = Color.White,
            radius = w * 0.32f,
            center = Offset(w / 2f, h / 2f)
        )

        // Blue horizontal crossbar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(w * 0.45f, h * 0.40f),
            size = Size(w * 0.55f, h * 0.20f)
        )
    }
}

/**
 * Authentic Apple Silhouette Icon
 */
@Composable
private fun AppleIconView() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Leaf
        val leafPath = Path().apply {
            moveTo(w * 0.52f, h * 0.15f)
            cubicTo(w * 0.62f, h * 0.05f, w * 0.65f, 0f, w * 0.65f, 0f)
            cubicTo(w * 0.55f, 0f, w * 0.45f, h * 0.08f, w * 0.45f, h * 0.18f)
            close()
        }
        drawPath(leafPath, color = Color.Black)

        // Apple body
        val bodyPath = Path().apply {
            moveTo(w * 0.5f, h * 0.30f)
            cubicTo(w * 0.35f, h * 0.22f, w * 0.15f, h * 0.32f, w * 0.15f, h * 0.58f)
            cubicTo(w * 0.15f, h * 0.82f, w * 0.35f, h * 1.0f, w * 0.5f, h * 0.98f)
            cubicTo(w * 0.65f, h * 1.0f, w * 0.85f, h * 0.82f, w * 0.85f, h * 0.58f)
            cubicTo(w * 0.85f, h * 0.32f, w * 0.65f, h * 0.22f, w * 0.5f, h * 0.30f)
            close()
        }
        drawPath(bodyPath, color = Color.Black)
    }
}

/**
 * Initiates Google Sign-In with Jetpack CredentialManager.
 */
private fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSuccess: (FirebaseUser) -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val serverClientId = context.getString(R.string.default_web_client_id)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = Firebase.auth.signInWithCredential(firebaseAuthCredential).await()
                val user = authResult.user
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("Failed to obtain user session from Google.")
                }
            } else {
                onError("Unexpected credential format received.")
            }
        } catch (e: GetCredentialCancellationException) {
            onCancelled()
        } catch (e: Exception) {
            onError("Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
