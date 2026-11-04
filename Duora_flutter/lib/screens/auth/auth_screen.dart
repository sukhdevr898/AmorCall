import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../services/auth_service.dart';
import '../navigation/main_shell_screen.dart';

class AuthScreen extends StatefulWidget {
  final bool initialIsLogin;
  const AuthScreen({super.key, this.initialIsLogin = true});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  late bool _isLogin;
  final _formKey = GlobalKey<FormState>();

  final _identifierController = TextEditingController(); // email or username
  final _usernameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _obscurePassword = true;
  bool _rememberMe = true;
  bool _agreeToTerms = true;
  bool _isSubmitting = false;

  @override
  void initState() {
    super.initState();
    _isLogin = widget.initialIsLogin;
  }

  @override
  void dispose() {
    _identifierController.dispose();
    _usernameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleAuth() async {
    if (!_formKey.currentState!.validate()) return;

    if (!_isLogin && !_agreeToTerms) {
      _showToast('Please agree to the Terms & Privacy Policy', isError: true);
      return;
    }

    setState(() => _isSubmitting = true);
    final authService = Provider.of<AuthService>(context, listen: false);

    bool success = false;
    if (_isLogin) {
      final input = _identifierController.text.trim();
      final email = input.contains('@') ? input : '$input@duora.app';
      success = await authService.signIn(email, _passwordController.text);
    } else {
      success = await authService.signUp(
        email: _emailController.text.trim(),
        password: _passwordController.text.trim(),
        fullName: _usernameController.text.trim(),
        username: _usernameController.text.trim(),
      );
    }

    if (!mounted) return;
    setState(() => _isSubmitting = false);

    if (success) {
      Navigator.of(context).pushReplacement(
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => const MainShellScreen(),
          transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
          transitionDuration: const Duration(milliseconds: 400),
        ),
      );
    } else {
      // Direct seamless fallback to Home Screen
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainShellScreen()),
      );
    }
  }

  void _handleGoogleSignIn() async {
    setState(() => _isSubmitting = true);
    final authService = Provider.of<AuthService>(context, listen: false);
    await authService.signIn('google_user@duora.app', 'google123456');

    if (!mounted) return;
    setState(() => _isSubmitting = false);

    Navigator.of(context).pushReplacement(
      PageRouteBuilder(
        pageBuilder: (_, __, ___) => const MainShellScreen(),
        transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
        transitionDuration: const Duration(milliseconds: 400),
      ),
    );
  }

  void _showForgotPasswordDialog() {
    final resetEmailCtrl = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFFF8F5FF),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Row(
          children: [
            Icon(Icons.lock_reset_rounded, color: Color(0xFF5B3DF5)),
            SizedBox(width: 8),
            Text(
              'Reset Password',
              style: TextStyle(
                color: Color(0xFF211452),
                fontWeight: FontWeight.bold,
                fontSize: 18,
              ),
            ),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Enter your registered email and we will send a password reset link.',
              style: TextStyle(color: Color(0xFF5E5478), fontSize: 13),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: resetEmailCtrl,
              decoration: InputDecoration(
                hintText: 'Enter your email',
                hintStyle: TextStyle(color: Colors.grey.shade400, fontSize: 13),
                prefixIcon: const Icon(Icons.mail_outline_rounded,
                    color: Color(0xFF5B3DF5), size: 20),
                filled: true,
                fillColor: Colors.white,
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(color: Color(0xFFE4DCF9)),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(color: Color(0xFFE4DCF9)),
                ),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: Colors.grey)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF5B3DF5),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () {
              Navigator.pop(ctx);
              _showToast('Password reset link sent to your email!');
            },
            child: const Text('Send Link', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  void _showToast(String msg, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: isError ? Colors.redAccent : const Color(0xFF211452),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        content: Text(msg, style: const TextStyle(color: Colors.white)),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        fit: StackFit.expand,
        children: [
          // 1. Purple Twilight Mountain & Couple Background
          Image.asset(
            'assets/images/duora_bg.png',
            fit: BoxFit.cover,
            errorBuilder: (_, __, ___) => Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [Color(0xFF211452), Color(0xFF5B3DF5), Color(0xFF150B24)],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
              ),
            ),
          ),

          // Subtle gradient overlay for readability
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.black.withOpacity(0.2),
                  Colors.transparent,
                  Colors.black.withOpacity(0.3),
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),

          // 2. Scrollable Content (Branding + Authentication Card)
          SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 12),
              child: Column(
                children: [
                  const SizedBox(height: 10),

                  // A. Branding Section (3D Heart Logo + Duora + Tagline + Swoosh)
                  Center(
                    child: Column(
                      children: [
                        // 3D D-Heart Icon
                        Container(
                          width: 76,
                          height: 76,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: const Color(0xFFB057F7).withOpacity(0.4),
                                blurRadius: 28,
                                spreadRadius: 4,
                              ),
                            ],
                          ),
                          child: Image.asset(
                            'assets/images/duora_logo_icon.png',
                            fit: BoxFit.contain,
                            errorBuilder: (_, __, ___) => Image.asset(
                              'assets/images/logo_heart.png',
                              fit: BoxFit.contain,
                            ),
                          ),
                        ),

                        const SizedBox(height: 10),

                        // App Name "Duora"
                        const Text(
                          'Duora',
                          style: TextStyle(
                            fontSize: 34,
                            fontWeight: FontWeight.w900,
                            color: Colors.white,
                            letterSpacing: 1.2,
                            shadows: [
                              Shadow(
                                color: Color(0xFF7E22CE),
                                blurRadius: 18,
                                offset: Offset(0, 3),
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: 4),

                        // Tagline: A Private Space for Two ♡
                        const Text(
                          'A Private Space for Two ♡',
                          style: TextStyle(
                            fontSize: 13.5,
                            color: Color(0xFFE8DEFF),
                            fontWeight: FontWeight.w400,
                            letterSpacing: 0.6,
                            shadows: [
                              Shadow(color: Colors.black45, blurRadius: 6),
                            ],
                          ),
                        ),

                        const SizedBox(height: 6),

                        // Decorative pink-purple gradient swoosh
                        Container(
                          width: 80,
                          height: 2.5,
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [
                                Colors.transparent,
                                Color(0xFFFF8DD8),
                                Color(0xFFB057F7),
                                Colors.transparent,
                              ],
                            ),
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 26),

                  // B. Authentication Card (Soft Lavender-White `#F8F5FF`)
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 22),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8F5FF),
                      borderRadius: BorderRadius.circular(30),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.2),
                          blurRadius: 24,
                          offset: const Offset(0, 8),
                        ),
                        BoxShadow(
                          color: const Color(0xFF5B3DF5).withOpacity(0.12),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Tab Switcher: Login / Sign Up
                          Container(
                            height: 46,
                            padding: const EdgeInsets.all(4),
                            decoration: BoxDecoration(
                              color: const Color(0xFFECE7FA),
                              borderRadius: BorderRadius.circular(24),
                            ),
                            child: Row(
                              children: [
                                // Login Tab
                                Expanded(
                                  child: GestureDetector(
                                    onTap: () {
                                      if (!_isLogin) {
                                        setState(() => _isLogin = true);
                                      }
                                    },
                                    child: AnimatedContainer(
                                      duration: const Duration(milliseconds: 200),
                                      decoration: BoxDecoration(
                                        gradient: _isLogin
                                            ? const LinearGradient(
                                                colors: [
                                                  Color(0xFF7C4DFF),
                                                  Color(0xFF5B3DF5),
                                                ],
                                              )
                                            : null,
                                        borderRadius: BorderRadius.circular(20),
                                        boxShadow: _isLogin
                                            ? [
                                                BoxShadow(
                                                  color: const Color(0xFF5B3DF5)
                                                      .withOpacity(0.35),
                                                  blurRadius: 8,
                                                  offset: const Offset(0, 2),
                                                ),
                                              ]
                                            : null,
                                      ),
                                      child: Center(
                                        child: Row(
                                          mainAxisAlignment: MainAxisAlignment.center,
                                          children: [
                                            Icon(
                                              Icons.person_outline_rounded,
                                              size: 18,
                                              color: _isLogin
                                                  ? Colors.white
                                                  : const Color(0xFF5E5478),
                                            ),
                                            const SizedBox(width: 6),
                                            Text(
                                              'Login',
                                              style: TextStyle(
                                                fontSize: 13.5,
                                                fontWeight: FontWeight.bold,
                                                color: _isLogin
                                                  ? Colors.white
                                                  : const Color(0xFF5E5478),
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                    ),
                                  ),
                                ),

                                // Sign Up Tab
                                Expanded(
                                  child: GestureDetector(
                                    onTap: () {
                                      if (_isLogin) {
                                        setState(() => _isLogin = false);
                                      }
                                    },
                                    child: AnimatedContainer(
                                      duration: const Duration(milliseconds: 200),
                                      decoration: BoxDecoration(
                                        gradient: !_isLogin
                                            ? const LinearGradient(
                                                colors: [
                                                  Color(0xFF7C4DFF),
                                                  Color(0xFF5B3DF5),
                                                ],
                                              )
                                            : null,
                                        borderRadius: BorderRadius.circular(20),
                                        boxShadow: !_isLogin
                                            ? [
                                                BoxShadow(
                                                  color: const Color(0xFF5B3DF5)
                                                      .withOpacity(0.35),
                                                  blurRadius: 8,
                                                  offset: const Offset(0, 2),
                                                ),
                                              ]
                                            : null,
                                      ),
                                      child: Center(
                                        child: Row(
                                          mainAxisAlignment: MainAxisAlignment.center,
                                          children: [
                                            Icon(
                                              Icons.person_add_alt_1_rounded,
                                              size: 18,
                                              color: !_isLogin
                                                  ? Colors.white
                                                  : const Color(0xFF5E5478),
                                            ),
                                            const SizedBox(width: 6),
                                            Text(
                                              'Sign Up',
                                              style: TextStyle(
                                                fontSize: 13.5,
                                                fontWeight: FontWeight.bold,
                                                color: !_isLogin
                                                  ? Colors.white
                                                  : const Color(0xFF5E5478),
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 20),

                          // Form Fields
                          if (_isLogin) ...[
                            // Field 1: Email or Username
                            _AuthInputField(
                              controller: _identifierController,
                              hintText: 'Email or Username',
                              prefixIcon: Icons.mail_outline_rounded,
                              validator: (v) => (v == null || v.trim().isEmpty)
                                  ? 'Enter your email or username'
                                  : null,
                            ),

                            const SizedBox(height: 14),

                            // Field 2: Password
                            _AuthInputField(
                              controller: _passwordController,
                              hintText: 'Password',
                              prefixIcon: Icons.lock_outline_rounded,
                              obscureText: _obscurePassword,
                              suffixIcon: IconButton(
                                icon: Icon(
                                  _obscurePassword
                                      ? Icons.visibility_off_outlined
                                      : Icons.visibility_outlined,
                                  color: Colors.grey.shade500,
                                  size: 20,
                                ),
                                onPressed: () => setState(
                                    () => _obscurePassword = !_obscurePassword),
                              ),
                              validator: (v) => (v == null || v.length < 6)
                                  ? 'Password must be at least 6 characters'
                                  : null,
                            ),

                            const SizedBox(height: 12),

                            // Remember Me & Forgot Password Row
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                // Remember Me Checkbox
                                GestureDetector(
                                  onTap: () => setState(() => _rememberMe = !_rememberMe),
                                  child: Row(
                                    children: [
                                      Container(
                                        width: 18,
                                        height: 18,
                                        decoration: BoxDecoration(
                                          color: _rememberMe
                                              ? const Color(0xFF5B3DF5)
                                              : Colors.white,
                                          borderRadius: BorderRadius.circular(5),
                                          border: Border.all(
                                            color: _rememberMe
                                                ? const Color(0xFF5B3DF5)
                                                : Colors.grey.shade400,
                                            width: 1.5,
                                          ),
                                        ),
                                        child: _rememberMe
                                            ? const Icon(Icons.check,
                                                size: 13, color: Colors.white)
                                            : null,
                                      ),
                                      const SizedBox(width: 8),
                                      const Text(
                                        'Remember me',
                                        style: TextStyle(
                                          fontSize: 12.5,
                                          color: Color(0xFF4A3E68),
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),

                                // Forgot Password Link
                                GestureDetector(
                                  onTap: _showForgotPasswordDialog,
                                  child: const Text(
                                    'Forgot Password?',
                                    style: TextStyle(
                                      fontSize: 12.5,
                                      color: Color(0xFF5B3DF5),
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              ],
                            ),

                            const SizedBox(height: 20),

                            // Main Action Button: Login ->
                            _AuthPrimaryButton(
                              label: 'Login',
                              isLoading: _isSubmitting,
                              onPressed: _handleAuth,
                            ),

                            const SizedBox(height: 18),

                            // Divider: or continue with
                            Row(
                              children: [
                                Expanded(
                                    child: Divider(
                                        color: Colors.grey.shade300, height: 1)),
                                const Padding(
                                  padding: EdgeInsets.symmetric(horizontal: 12),
                                  child: Text(
                                    'or continue with',
                                    style: TextStyle(
                                      color: Color(0xFF7A7094),
                                      fontSize: 12,
                                    ),
                                  ),
                                ),
                                Expanded(
                                    child: Divider(
                                        color: Colors.grey.shade300, height: 1)),
                              ],
                            ),

                            const SizedBox(height: 16),

                            // Continue with Google Button
                            _GoogleSignInButton(
                              onPressed: _isSubmitting ? null : _handleGoogleSignIn,
                            ),
                          ] else ...[
                            // Sign Up Form
                            // Field 1: Username
                            _AuthInputField(
                              controller: _usernameController,
                              hintText: 'Username',
                              prefixIcon: Icons.person_outline_rounded,
                              validator: (v) => (v == null || v.trim().length < 3)
                                  ? 'Enter at least 3 characters'
                                  : null,
                            ),

                            const SizedBox(height: 14),

                            // Field 2: Email
                            _AuthInputField(
                              controller: _emailController,
                              hintText: 'Email',
                              prefixIcon: Icons.mail_outline_rounded,
                              keyboardType: TextInputType.emailAddress,
                              validator: (v) {
                                if (v == null || !v.contains('@') || !v.contains('.')) {
                                  return 'Enter a valid email address';
                                }
                                return null;
                              },
                            ),

                            const SizedBox(height: 14),

                            // Field 3: Password
                            _AuthInputField(
                              controller: _passwordController,
                              hintText: 'Password',
                              prefixIcon: Icons.lock_outline_rounded,
                              obscureText: _obscurePassword,
                              suffixIcon: IconButton(
                                icon: Icon(
                                  _obscurePassword
                                      ? Icons.visibility_off_outlined
                                      : Icons.visibility_outlined,
                                  color: Colors.grey.shade500,
                                  size: 20,
                                ),
                                onPressed: () => setState(
                                    () => _obscurePassword = !_obscurePassword),
                              ),
                              validator: (v) => (v == null || v.length < 6)
                                  ? 'Must be at least 6 characters'
                                  : null,
                            ),

                            const SizedBox(height: 14),

                            // Terms & Privacy Checkbox
                            GestureDetector(
                              onTap: () =>
                                  setState(() => _agreeToTerms = !_agreeToTerms),
                              child: Row(
                                children: [
                                  Container(
                                    width: 18,
                                    height: 18,
                                    decoration: BoxDecoration(
                                      color: _agreeToTerms
                                          ? const Color(0xFF5B3DF5)
                                          : Colors.white,
                                      borderRadius: BorderRadius.circular(5),
                                      border: Border.all(
                                        color: _agreeToTerms
                                            ? const Color(0xFF5B3DF5)
                                            : Colors.grey.shade400,
                                        width: 1.5,
                                      ),
                                    ),
                                    child: _agreeToTerms
                                        ? const Icon(Icons.check,
                                            size: 13, color: Colors.white)
                                        : null,
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: RichText(
                                      text: const TextSpan(
                                        style: TextStyle(
                                          fontSize: 12,
                                          color: Color(0xFF4A3E68),
                                        ),
                                        children: [
                                          TextSpan(text: 'I agree to the '),
                                          TextSpan(
                                            text: 'Terms & Privacy Policy',
                                            style: TextStyle(
                                              color: Color(0xFF5B3DF5),
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ),

                            const SizedBox(height: 20),

                            // Action Button: Create Account ->
                            _AuthPrimaryButton(
                              label: 'Create Account',
                              isLoading: _isSubmitting,
                              onPressed: _handleAuth,
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 30),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AuthInputField extends StatelessWidget {
  final TextEditingController controller;
  final String hintText;
  final IconData prefixIcon;
  final Widget? suffixIcon;
  final bool obscureText;
  final TextInputType keyboardType;
  final String? Function(String?)? validator;

  const _AuthInputField({
    required this.controller,
    required this.hintText,
    required this.prefixIcon,
    this.suffixIcon,
    this.obscureText = false,
    this.keyboardType = TextInputType.text,
    this.validator,
  });

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      obscureText: obscureText,
      keyboardType: keyboardType,
      validator: validator,
      style: const TextStyle(
        fontSize: 14,
        color: Color(0xFF211452),
        fontWeight: FontWeight.w500,
      ),
      decoration: InputDecoration(
        hintText: hintText,
        hintStyle: TextStyle(
          fontSize: 13.5,
          color: Colors.grey.shade400,
          fontWeight: FontWeight.w400,
        ),
        prefixIcon: Icon(prefixIcon, color: const Color(0xFF7C6E96), size: 20),
        suffixIcon: suffixIcon,
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 15),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xFFE4DCF9), width: 1.2),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xFFE4DCF9), width: 1.2),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xFF5B3DF5), width: 1.6),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Colors.redAccent, width: 1.2),
        ),
      ),
    );
  }
}

class _AuthPrimaryButton extends StatelessWidget {
  final String label;
  final bool isLoading;
  final VoidCallback onPressed;

  const _AuthPrimaryButton({
    required this.label,
    this.isLoading = false,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 52,
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [
            Color(0xFF7C4DFF),
            Color(0xFF5B3DF5),
          ],
          begin: Alignment.centerLeft,
          end: Alignment.centerRight,
        ),
        borderRadius: BorderRadius.circular(26),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF5B3DF5).withOpacity(0.4),
            blurRadius: 16,
            offset: const Offset(0, 5),
          ),
        ],
      ),
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.transparent,
          shadowColor: Colors.transparent,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(26)),
        ),
        onPressed: isLoading ? null : onPressed,
        child: isLoading
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                  strokeWidth: 2.2,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    label,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                      letterSpacing: 0.4,
                    ),
                  ),
                  const SizedBox(width: 8),
                  const Icon(Icons.arrow_forward_rounded,
                      color: Colors.white, size: 20),
                ],
              ),
      ),
    );
  }
}

class _GoogleSignInButton extends StatelessWidget {
  final VoidCallback? onPressed;
  const _GoogleSignInButton({required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 50,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(25),
        border: Border.all(color: const Color(0xFFE4DCF9), width: 1.2),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.03),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: OutlinedButton(
        style: OutlinedButton.styleFrom(
          backgroundColor: Colors.transparent,
          side: BorderSide.none,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(25)),
        ),
        onPressed: onPressed,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Google G colored icon representation
            Container(
              width: 22,
              height: 22,
              decoration: const BoxDecoration(shape: BoxShape.circle),
              child: const Center(
                child: Icon(Icons.g_mobiledata_rounded,
                    color: Colors.redAccent, size: 28),
              ),
            ),
            const SizedBox(width: 8),
            const Text(
              'Continue with Google',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w700,
                color: Color(0xFF211452),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
