import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/user_profile.dart';

class AuthService extends ChangeNotifier {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  UserProfile? _currentUser;
  bool _isLoading = false;
  String? _errorMessage;

  UserProfile? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _currentUser != null;
  bool get isPaired => _currentUser?.isPaired ?? false;

  /// Generate a unique 6-character uppercase alphanumeric pair code (e.g. A9F2X1)
  String generatePairCode() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    final random = Random();
    return List.generate(6, (index) => chars[random.nextInt(chars.length)]).join();
  }

  /// Initialize session from stored preferences or Firebase User
  Future<void> checkAuthStatus() async {
    _isLoading = true;
    notifyListeners();

    try {
      final prefs = await SharedPreferences.getInstance();
      final savedUid = prefs.getString('user_uid');

      final user = _auth.currentUser;
      final targetUid = user?.uid ?? savedUid;

      if (targetUid != null && targetUid.isNotEmpty) {
        final doc = await _firestore.collection('users').doc(targetUid).get();
        if (doc.exists && doc.data() != null) {
          _currentUser = UserProfile.fromMap(doc.data()!, targetUid);
        } else {
          // Fallback to local profile cache
          _currentUser = UserProfile(
            uid: targetUid,
            email: prefs.getString('user_email') ?? 'user@duora.app',
            username: prefs.getString('user_username') ?? 'duora_user',
            fullName: prefs.getString('user_fullname') ?? 'You',
            myPairCode: prefs.getString('my_pair_code') ?? generatePairCode(),
            isPaired: prefs.getBool('is_paired') ?? false,
            partnerName: prefs.getString('partner_name') ?? 'My Partner',
            partnerPairCode: prefs.getString('partner_pair_code') ?? '',
          );
        }
      }
    } catch (e) {
      debugPrint('Error restoring auth status: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Sign In with Email & Password
  Future<bool> signIn(String email, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final credential = await _auth.signInWithEmailAndPassword(
        email: email.trim(),
        password: password.trim(),
      );

      final uid = credential.user!.uid;
      final doc = await _firestore.collection('users').doc(uid).get();

      if (doc.exists && doc.data() != null) {
        _currentUser = UserProfile.fromMap(doc.data()!, uid);
      } else {
        // Create initial user document if not found
        final pairCode = generatePairCode();
        _currentUser = UserProfile(
          uid: uid,
          email: email.trim(),
          username: email.split('@').first,
          fullName: 'You',
          myPairCode: pairCode,
        );
        await _firestore.collection('users').doc(uid).set(_currentUser!.toMap());
      }

      await _persistLocalSession(_currentUser!);
      _isLoading = false;
      notifyListeners();
      return true;
    } on FirebaseAuthException catch (e) {
      _errorMessage = e.message ?? 'Authentication failed.';
      _isLoading = false;
      notifyListeners();
      return false;
    } catch (e) {
      _errorMessage = 'An unexpected error occurred. Please try again.';
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Sign Up with Email, Password, and Full Name
  Future<bool> signUp({
    required String email,
    required String password,
    required String fullName,
    required String username,
  }) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final credential = await _auth.createUserWithEmailAndPassword(
        email: email.trim(),
        password: password.trim(),
      );

      final uid = credential.user!.uid;
      final pairCode = generatePairCode();

      _currentUser = UserProfile(
        uid: uid,
        email: email.trim(),
        username: username.trim(),
        fullName: fullName.trim(),
        myPairCode: pairCode,
      );

      // Save to Firestore
      await _firestore.collection('users').doc(uid).set(_currentUser!.toMap());
      // Register pairing code mapping
      await _firestore.collection('pair_codes').doc(pairCode).set({
        'uid': uid,
        'username': username.trim(),
        'fullName': fullName.trim(),
      });

      await _persistLocalSession(_currentUser!);
      _isLoading = false;
      notifyListeners();
      return true;
    } on FirebaseAuthException catch (e) {
      _errorMessage = e.message ?? 'Registration failed.';
      _isLoading = false;
      notifyListeners();
      return false;
    } catch (e) {
      _errorMessage = 'Unable to create account. Check your network.';
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  /// Save session to local storage for fast startup
  Future<void> _persistLocalSession(UserProfile user) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('user_uid', user.uid);
    await prefs.setString('user_email', user.email);
    await prefs.setString('user_username', user.username);
    await prefs.setString('user_fullname', user.fullName);
    await prefs.setString('my_pair_code', user.myPairCode);
    await prefs.setBool('is_paired', user.isPaired);
    await prefs.setString('partner_name', user.partnerName);
    await prefs.setString('partner_pair_code', user.partnerPairCode);
  }

  /// Sign Out
  Future<void> signOut() async {
    await _auth.signOut();
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    _currentUser = null;
    notifyListeners();
  }
}
