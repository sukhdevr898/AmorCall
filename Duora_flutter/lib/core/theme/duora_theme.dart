import 'package:flutter/material.dart';

class DuoraColors {
  // Primary brand palette (Romantic, modern & vibrant)
  static const Color primaryPink = Color(0xFFFF2D8D);
  static const Color vibrantRose = Color(0xFFFF4B91);
  static const Color softPink = Color(0xFFFF75A0);
  static const Color heartRed = Color(0xFFFF3366);
  static const Color romanticPurple = Color(0xFF7E22CE);
  
  // Luxury Dark & Glass backgrounds
  static const Color background = Color(0xFF0D0A14);
  static const Color surface = Color(0xFF161224);
  static const Color surfaceCard = Color(0xFF1E1730);
  static const Color surfaceHighlight = Color(0xFF281E40);
  static const Color inputBackground = Color(0xFF140F22);
  
  // Borders & Accents
  static const Color border = Color(0x33FF2D8D);
  static const Color borderMuted = Color(0x22FFFFFF);
  static const Color divider = Color(0x1AFFFFFF);
  
  // Status Colors
  static const Color onlineGreen = Color(0xFF10B981);
  static const Color verifiedBlue = Color(0xFF38BDF8);
  static const Color adminGold = Color(0xFFFBBF24);
  static const Color warning = Color(0xFFF59E0B);
  static const Color error = Color(0xFFEF4444);
  
  // Text Colors
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFFB3ADCA);
  static const Color textMuted = Color(0xFF7D769A);
  
  // Gradients
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [primaryPink, vibrantRose],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient luxuryBackgroundGradient = LinearGradient(
    colors: [Color(0xFF150B24), Color(0xFF0B0714)],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  );

  static const LinearGradient glassGradient = LinearGradient(
    colors: [Color(0x33FF2D8D), Color(0x11161224)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}

class DuoraTheme {
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: DuoraColors.background,
      primaryColor: DuoraColors.primaryPink,
      colorScheme: const ColorScheme.dark(
        primary: DuoraColors.primaryPink,
        secondary: DuoraColors.vibrantRose,
        surface: DuoraColors.surface,
        background: DuoraColors.background,
        error: DuoraColors.error,
        onPrimary: Colors.white,
        onSurface: DuoraColors.textPrimary,
      ),
      fontFamily: 'Inter',
      cardTheme: CardTheme(
        color: DuoraColors.surfaceCard,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: const BorderSide(color: DuoraColors.borderMuted, width: 1),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: DuoraColors.inputBackground,
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
        hintStyle: const TextStyle(color: DuoraColors.textMuted, fontSize: 14),
        labelStyle: const TextStyle(color: DuoraColors.textSecondary, fontSize: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: DuoraColors.borderMuted),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: DuoraColors.borderMuted),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: DuoraColors.primaryPink, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: DuoraColors.error),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: DuoraColors.primaryPink,
          foregroundColor: Colors.white,
          minimumSize: const Size(double.infinity, 54),
          elevation: 4,
          shadowColor: DuoraColors.primaryPink.withOpacity(0.4),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          textStyle: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.3,
          ),
        ),
      ),
    );
  }
}
