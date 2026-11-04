import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../services/auth_service.dart';
import '../navigation/main_shell_screen.dart';
import '../auth/auth_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;
  Timer? _timer;
  bool _navigated = false;

  @override
  void initState() {
    super.initState();

    _fadeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _fadeController, curve: Curves.easeIn),
    );

    _scaleAnimation = Tween<double>(begin: 0.88, end: 1.0).animate(
      CurvedAnimation(parent: _fadeController, curve: Curves.easeOutBack),
    );

    _fadeController.forward();

    // Check auth in background safely
    final authService = Provider.of<AuthService>(context, listen: false);
    authService.checkAuthStatus();

    // GUARANTEED Navigation after 2.4s — cannot get stuck
    _timer = Timer(const Duration(milliseconds: 2400), _proceedToNextScreen);
  }

  void _proceedToNextScreen() {
    if (_navigated || !mounted) return;
    _navigated = true;
    _timer?.cancel();

    final authService = Provider.of<AuthService>(context, listen: false);

    // If authenticated or demo, transition seamlessly
    Navigator.of(context).pushReplacement(
      PageRouteBuilder(
        pageBuilder: (_, __, ___) => authService.isAuthenticated
            ? const MainShellScreen()
            : const AuthScreen(initialIsLogin: true),
        transitionsBuilder: (_, animation, __, child) {
          return FadeTransition(opacity: animation, child: child);
        },
        transitionDuration: const Duration(milliseconds: 500),
      ),
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: _proceedToNextScreen, // Instant tap-through fallback
      child: Scaffold(
        body: Stack(
          fit: StackFit.expand,
          children: [
            // 1. Romantic Twilight Purple Mountain & Floating Hearts Background
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

            // Subtle dark twilight gradient overlay
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Colors.black.withOpacity(0.25),
                    Colors.transparent,
                    Colors.black.withOpacity(0.65),
                  ],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
              ),
            ),

            // 2. Center Branding & App Identity
            SafeArea(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Spacer(flex: 3),

                  FadeTransition(
                    opacity: _fadeAnimation,
                    child: ScaleTransition(
                      scale: _scaleAnimation,
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          // 3D Glossy Dual D-Heart Logo
                          Container(
                            width: 140,
                            height: 140,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFFB057F7).withOpacity(0.45),
                                  blurRadius: 44,
                                  spreadRadius: 8,
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

                          const SizedBox(height: 24),

                          // App Name "Duora" in bold typography
                          const Text(
                            'Duora',
                            style: TextStyle(
                              fontSize: 42,
                              fontWeight: FontWeight.w900,
                              color: Colors.white,
                              letterSpacing: 1.5,
                              shadows: [
                                Shadow(
                                  color: Color(0xFF7E22CE),
                                  blurRadius: 20,
                                  offset: Offset(0, 4),
                                ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 8),

                          // Tagline: More Than Just a Chat ♡
                          const Text(
                            'More Than Just a Chat ♡',
                            style: TextStyle(
                              fontSize: 16,
                              color: Color(0xFFE8DEFF),
                              fontWeight: FontWeight.w400,
                              letterSpacing: 0.8,
                              shadows: [
                                Shadow(color: Colors.black45, blurRadius: 8),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const Spacer(flex: 4),

                  // 3. Bottom Loading Indicator & "Connecting You..."
                  const SizedBox(
                    width: 28,
                    height: 28,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.8,
                      valueColor: AlwaysStoppedAnimation<Color>(Color(0xFFFFB6D5)),
                    ),
                  ),

                  const SizedBox(height: 14),

                  const Text(
                    'Connecting You...',
                    style: TextStyle(
                      fontSize: 14,
                      color: Color(0xFFE8DEFF),
                      fontWeight: FontWeight.w500,
                      letterSpacing: 0.5,
                    ),
                  ),

                  const SizedBox(height: 48),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
