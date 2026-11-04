import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';
import '../auth/auth_screen.dart';
import '../navigation/main_shell_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _progressController;
  late Animation<double> _progressAnimation;
  bool _navigated = false;

  @override
  void initState() {
    super.initState();

    _progressController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2600),
    );

    _progressAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _progressController, curve: Curves.easeInOut),
    );

    _progressController.forward();

    _startAppFlow();
  }

  Future<void> _startAppFlow() async {
    // Guaranteed non-blocking auth check
    try {
      final authService = Provider.of<AuthService>(context, listen: false);
      await authService.checkAuthStatus().timeout(
        const Duration(milliseconds: 2000),
        onTimeout: () {
          debugPrint('Auth check timed out safely. Continuing to app.');
        },
      );
    } catch (e) {
      debugPrint('Safe auth check exception: $e');
    }

    // Wait until progress bar completes
    await Future.delayed(const Duration(milliseconds: 2700));

    if (!mounted || _navigated) return;
    _navigated = true;

    final authService = Provider.of<AuthService>(context, listen: false);

    if (authService.isAuthenticated) {
      Navigator.of(context).pushReplacement(
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => const MainShellScreen(),
          transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
          transitionDuration: const Duration(milliseconds: 400),
        ),
      );
    } else {
      Navigator.of(context).pushReplacement(
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => const AuthScreen(initialIsLogin: true),
          transitionsBuilder: (_, a, __, c) => FadeTransition(opacity: a, child: c),
          transitionDuration: const Duration(milliseconds: 400),
        ),
      );
    }
  }

  @override
  void dispose() {
    _progressController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;

    return Scaffold(
      body: Stack(
        fit: StackFit.expand,
        children: [
          // 1. Romantic couple sunset in flower field background
          Image.asset(
            'assets/images/bg_splash.png',
            fit: BoxFit.cover,
            errorBuilder: (_, __, ___) => Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [Color(0xFFFFB6C1), Color(0xFFFF69B4), Color(0xFFFF1493)],
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                ),
              ),
            ),
          ),

          // 2. Soft romantic pink gradient overlay
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.white.withOpacity(0.1),
                  Colors.black.withOpacity(0.2),
                  Colors.black.withOpacity(0.55),
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),

          // 3. Center Branding: Glossy Dual Heart & Duora Logo
          SafeArea(
            child: Column(
              children: [
                const Spacer(flex: 3),

                // Glossy Dual 3D Heart Symbol
                Container(
                  width: 140,
                  height: 140,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFFFF2D8D).withOpacity(0.45),
                        blurRadius: 40,
                        spreadRadius: 10,
                      ),
                    ],
                  ),
                  child: Image.asset(
                    'assets/images/logo_heart.png',
                    fit: BoxFit.contain,
                    errorBuilder: (_, __, ___) => const Icon(
                      Icons.favorite_rounded,
                      size: 90,
                      color: Color(0xFFFF2D8D),
                    ),
                  ),
                ),

                const SizedBox(height: 24),

                // Duora Always Together Wordmark
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  height: 60,
                  child: Image.asset(
                    'assets/images/logo_full_duora.png',
                    fit: BoxFit.contain,
                    errorBuilder: (_, __, ___) => const Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'Duora',
                          style: TextStyle(
                            fontSize: 36,
                            fontWeight: FontWeight.w900,
                            color: Colors.white,
                            letterSpacing: 1.5,
                          ),
                        ),
                        Text(
                          'Always Together',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            color: Colors.white70,
                            letterSpacing: 2.0,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

                const Spacer(flex: 4),

                // 4. Bottom Status & Pill Loading Progress Bar
                const Text(
                  'Bringing you closer...',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    letterSpacing: 0.5,
                    shadows: [
                      Shadow(color: Colors.black45, blurRadius: 8),
                    ],
                  ),
                ),

                const SizedBox(height: 16),

                // Pill-shaped animated progress bar
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 48),
                  child: Container(
                    height: 8,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.35),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: AnimatedBuilder(
                      animation: _progressAnimation,
                      builder: (context, _) {
                        return FractionallySizedBox(
                          alignment: Alignment.centerLeft,
                          widthFactor: _progressAnimation.value,
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: const LinearGradient(
                                colors: [Color(0xFFFF2D8D), Color(0xFFFF6584)],
                              ),
                              borderRadius: BorderRadius.circular(10),
                              boxShadow: [
                                BoxShadow(
                                  color: const Color(0xFFFF2D8D).withOpacity(0.8),
                                  blurRadius: 10,
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ),

                const SizedBox(height: 40),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
