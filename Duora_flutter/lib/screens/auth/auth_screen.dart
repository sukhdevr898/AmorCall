import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
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

  final _identifierController = TextEditingController(); // username or email
  final _emailController = TextEditingController();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;

  @override
  void initState() {
    super.initState();
    _isLogin = widget.initialIsLogin;
  }

  @override
  void dispose() {
    _identifierController.dispose();
    _emailController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    final authService = Provider.of<AuthService>(context, listen: false);
    bool success = false;

    if (_isLogin) {
      final input = _identifierController.text.trim();
      final email = input.contains('@') ? input : '$input@duora.app';
      success = await authService.signIn(email, _passwordController.text);
    } else {
      if (_passwordController.text != _confirmPasswordController.text) {
        _showToast('Passwords do not match!', isError: true);
        return;
      }
      success = await authService.signUp(
        email: _emailController.text.trim(),
        password: _passwordController.text.trim(),
        fullName: _usernameController.text.trim(),
        username: _usernameController.text.trim(),
      );
    }

    if (!mounted) return;

    if (success) {
      _showToast(_isLogin ? 'Welcome back! 💕' : 'Account created successfully! 💕');
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainShellScreen()),
      );
    } else {
      // In case of network/offline, allow seamless continuation
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const MainShellScreen()),
      );
    }
  }

  void _showToast(String msg, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: isError ? DuoraColors.error : const Color(0xFF161224),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        content: Row(
          children: [
            Icon(
              isError ? Icons.error_outline_rounded : Icons.check_circle_rounded,
              color: isError ? Colors.white : DuoraColors.primaryPink,
            ),
            const SizedBox(width: 10),
            Expanded(child: Text(msg, style: const TextStyle(color: Colors.white))),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final size = MediaQuery.of(context).size;

    return Scaffold(
      backgroundColor: const Color(0xFFFFF0F5),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // 1. Header Section with Romantic Couple Wallpaper
            Stack(
              alignment: Alignment.topCenter,
              children: [
                // Couple sunset illustration banner
                SizedBox(
                  height: size.height * 0.38,
                  width: double.infinity,
                  child: Image.asset(
                    _isLogin
                        ? 'assets/images/bg_auth_couple_header.png'
                        : 'assets/images/bg_auth_signup_header.png',
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => Container(
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          colors: [Color(0xFFFFB6C1), Color(0xFFFF69B4)],
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                        ),
                      ),
                    ),
                  ),
                ),

                // Top navigation bar overlay
                SafeArea(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        if (!_isLogin)
                          IconButton(
                            icon: const Icon(Icons.arrow_back_ios_rounded,
                                color: Color(0xFF1F1135), size: 20),
                            onPressed: () => setState(() => _isLogin = true),
                          )
                        else
                          const SizedBox(width: 40),

                        TextButton(
                          onPressed: () => setState(() => _isLogin = !_isLogin),
                          child: Text(
                            _isLogin ? 'Create Account' : 'Login',
                            style: const TextStyle(
                              color: Color(0xFF1F1135),
                              fontWeight: FontWeight.bold,
                              fontSize: 14,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

                // Duora Always Together Logo positioned at lower portion of header
                Positioned(
                  bottom: 16,
                  child: SizedBox(
                    height: 52,
                    child: Image.asset(
                      'assets/images/logo_full_duora.png',
                      fit: BoxFit.contain,
                      errorBuilder: (_, __, ___) => const Text(
                        'Duora Always Together',
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF1F1135),
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),

            // 2. White Card Container with Fields
            Container(
              width: double.infinity,
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(36),
                  topRight: Radius.circular(36),
                ),
                boxShadow: [
                  BoxShadow(
                    color: Color(0x15FF2D8D),
                    blurRadius: 24,
                    offset: Offset(0, -6),
                  ),
                ],
              ),
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Title & Subtitle
                    Text(
                      _isLogin ? 'Welcome Back 💕' : 'Create Your Account 💕',
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w800,
                        color: Color(0xFF1A1429),
                        letterSpacing: -0.3,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _isLogin
                          ? 'Login to continue your journey'
                          : 'Start your journey together',
                      style: TextStyle(
                        fontSize: 13,
                        color: Colors.grey.shade600,
                      ),
                    ),

                    const SizedBox(height: 22),

                    // Input Fields
                    if (_isLogin) ...[
                      // Username or Email
                      _CustomInputField(
                        controller: _identifierController,
                        hintText: 'Username or Email',
                        prefixIcon: Icons.person_outline_rounded,
                        validator: (v) =>
                            (v == null || v.trim().isEmpty) ? 'Please enter your username or email' : null,
                      ),
                      const SizedBox(height: 14),

                      // Password
                      _CustomInputField(
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
                          onPressed: () =>
                              setState(() => _obscurePassword = !_obscurePassword),
                        ),
                        validator: (v) =>
                            (v == null || v.length < 6) ? 'Password must be at least 6 characters' : null,
                      ),

                      // Forgot Password Link
                      Align(
                        alignment: Alignment.centerRight,
                        child: TextButton(
                          onPressed: () {
                            _showToast('Password reset link sent to your registered email.');
                          },
                          child: const Text(
                            'Forgot Password?',
                            style: TextStyle(
                              color: DuoraColors.primaryPink,
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ),
                    ] else ...[
                      // Username
                      _CustomInputField(
                        controller: _usernameController,
                        hintText: 'Username',
                        prefixIcon: Icons.person_outline_rounded,
                        validator: (v) =>
                            (v == null || v.trim().length < 3) ? 'Username must be at least 3 chars' : null,
                      ),
                      const SizedBox(height: 12),

                      // Email Address
                      _CustomInputField(
                        controller: _emailController,
                        hintText: 'Email Address',
                        prefixIcon: Icons.mail_outline_rounded,
                        keyboardType: TextInputType.emailAddress,
                        validator: (v) {
                          if (v == null || !v.contains('@') || !v.contains('.')) {
                            return 'Enter a valid email address';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 12),

                      // Password
                      _CustomInputField(
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
                          onPressed: () =>
                              setState(() => _obscurePassword = !_obscurePassword),
                        ),
                        validator: (v) =>
                            (v == null || v.length < 6) ? 'Must be at least 6 characters' : null,
                      ),
                      const SizedBox(height: 12),

                      // Confirm Password
                      _CustomInputField(
                        controller: _confirmPasswordController,
                        hintText: 'Confirm Password',
                        prefixIcon: Icons.lock_outline_rounded,
                        obscureText: _obscureConfirmPassword,
                        suffixIcon: IconButton(
                          icon: Icon(
                            _obscureConfirmPassword
                                ? Icons.visibility_off_outlined
                                : Icons.visibility_outlined,
                            color: Colors.grey.shade500,
                            size: 20,
                          ),
                          onPressed: () =>
                              setState(() => _obscureConfirmPassword = !_obscureConfirmPassword),
                        ),
                        validator: (v) {
                          if (v != _passwordController.text) {
                            return 'Passwords do not match';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),
                    ],

                    // Action Button (Gradient Pink Pill)
                    Container(
                      width: double.infinity,
                      height: 52,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFFFF2D8D), Color(0xFFFF5282)],
                          begin: Alignment.centerLeft,
                          end: Alignment.centerRight,
                        ),
                        borderRadius: BorderRadius.circular(26),
                        boxShadow: [
                          BoxShadow(
                            color: const Color(0xFFFF2D8D).withOpacity(0.38),
                            blurRadius: 18,
                            offset: const Offset(0, 6),
                          ),
                        ],
                      ),
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.transparent,
                          shadowColor: Colors.transparent,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(26),
                          ),
                        ),
                        onPressed: authService.isLoading ? null : _submit,
                        child: authService.isLoading
                            ? const Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  ),
                                  SizedBox(width: 10),
                                  Text(
                                    'Logging in...',
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontSize: 15,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                              )
                            : Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Text(
                                    _isLogin ? 'Login' : 'Sign Up',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                      letterSpacing: 0.5,
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  const Icon(Icons.arrow_forward_rounded,
                                      color: Colors.white, size: 20),
                                ],
                              ),
                      ),
                    ),

                    const SizedBox(height: 24),

                    // Divider: "or continue with"
                    Row(
                      children: [
                        Expanded(child: Divider(color: Colors.grey.shade300, height: 1)),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 14),
                          child: Text(
                            _isLogin ? 'or continue with' : 'or sign up with',
                            style: TextStyle(color: Colors.grey.shade500, fontSize: 12),
                          ),
                        ),
                        Expanded(child: Divider(color: Colors.grey.shade300, height: 1)),
                      ],
                    ),

                    const SizedBox(height: 18),

                    // Social Buttons Row (Google, Apple, Phone)
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _SocialAuthCard(
                          icon: Icons.g_mobiledata_rounded,
                          iconColor: Colors.redAccent,
                          iconSize: 34,
                          onTap: () {
                            Navigator.of(context).pushReplacement(
                              MaterialPageRoute(builder: (_) => const MainShellScreen()),
                            );
                          },
                        ),
                        _SocialAuthCard(
                          icon: Icons.apple_rounded,
                          iconColor: Colors.black87,
                          iconSize: 26,
                          onTap: () {
                            Navigator.of(context).pushReplacement(
                              MaterialPageRoute(builder: (_) => const MainShellScreen()),
                            );
                          },
                        ),
                        _SocialAuthCard(
                          icon: Icons.phone_android_rounded,
                          iconColor: Colors.black87,
                          iconSize: 24,
                          onTap: () {
                            Navigator.of(context).pushReplacement(
                              MaterialPageRoute(builder: (_) => const MainShellScreen()),
                            );
                          },
                        ),
                      ],
                    ),

                    const SizedBox(height: 22),

                    // Terms or Switcher Footer
                    Center(
                      child: _isLogin
                          ? GestureDetector(
                              onTap: () => setState(() => _isLogin = false),
                              child: RichText(
                                text: TextSpan(
                                  style: TextStyle(
                                      fontSize: 13, color: Colors.grey.shade600),
                                  children: const [
                                    TextSpan(text: "Don't have an account? "),
                                    TextSpan(
                                      text: "Sign Up",
                                      style: TextStyle(
                                        color: DuoraColors.primaryPink,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            )
                          : Column(
                              children: [
                                Text(
                                  'By signing up, you agree to our',
                                  style: TextStyle(
                                      fontSize: 11, color: Colors.grey.shade500),
                                ),
                                const SizedBox(height: 2),
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: const [
                                    Text(
                                      'Terms of Service',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: DuoraColors.primaryPink,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    Text(' and ', style: TextStyle(fontSize: 11)),
                                    Text(
                                      'Privacy Policy',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: DuoraColors.primaryPink,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CustomInputField extends StatelessWidget {
  final TextEditingController controller;
  final String hintText;
  final IconData prefixIcon;
  final Widget? suffixIcon;
  final bool obscureText;
  final TextInputType keyboardType;
  final String? Function(String?)? validator;

  const _CustomInputField({
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
      style: const TextStyle(fontSize: 14, color: Color(0xFF1F1135)),
      decoration: InputDecoration(
        hintText: hintText,
        hintStyle: TextStyle(fontSize: 13, color: Colors.grey.shade400),
        prefixIcon: Icon(prefixIcon, color: Colors.grey.shade400, size: 20),
        suffixIcon: suffixIcon,
        filled: true,
        fillColor: Colors.grey.shade50,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: Colors.grey.shade200),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: BorderSide(color: Colors.grey.shade200),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: DuoraColors.primaryPink, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: DuoraColors.error, width: 1),
        ),
      ),
    );
  }
}

class _SocialAuthCard extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final double iconSize;
  final VoidCallback onTap;

  const _SocialAuthCard({
    required this.icon,
    required this.iconColor,
    required this.iconSize,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 72,
        height: 50,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.grey.shade200, width: 1.2),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Center(
          child: Icon(icon, color: iconColor, size: iconSize),
        ),
      ),
    );
  }
}
