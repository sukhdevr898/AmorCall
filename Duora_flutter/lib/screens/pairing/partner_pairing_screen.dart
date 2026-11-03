import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import '../../core/theme/duora_theme.dart';
import '../../services/auth_service.dart';

class PartnerPairingScreen extends StatefulWidget {
  const PartnerPairingScreen({super.key});

  @override
  State<PartnerPairingScreen> createState() => _PartnerPairingScreenState();
}

class _PartnerPairingScreenState extends State<PartnerPairingScreen> {
  final _partnerCodeController = TextEditingController();
  bool _isPairing = false;

  @override
  void dispose() {
    _partnerCodeController.dispose();
    super.dispose();
  }

  void _linkPartner() async {
    final code = _partnerCodeController.text.trim().toUpperCase().replaceAll('#', '');
    final authService = Provider.of<AuthService>(context, listen: false);
    final myCode = authService.currentUser?.myPairCode ?? '';

    if (code.length < 6) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: DuoraColors.error,
          content: Text('Please enter a valid 6-character partner code.'),
        ),
      );
      return;
    }

    if (code == myCode) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: DuoraColors.error,
          content: Text('Self-pairing is not allowed! Enter your partner’s code.'),
        ),
      );
      return;
    }

    setState(() => _isPairing = true);

    // Simulate verification delay & update local user state
    await Future.delayed(const Duration(milliseconds: 1400));

    if (!mounted) return;
    setState(() => _isPairing = false);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: DuoraColors.onlineGreen,
        content: Row(
          children: [
            const Icon(Icons.favorite_rounded, color: Colors.white),
            const SizedBox(width: 10),
            Text('Successfully paired with #$code! ❤️'),
          ],
        ),
      ),
    );

    Navigator.pop(context);
  }

  void _openQrScanner() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.black,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => SizedBox(
        height: MediaQuery.of(ctx).size.height * 0.7,
        child: Stack(
          children: [
            MobileScanner(
              onDetect: (capture) {
                final List<Barcode> barcodes = capture.barcodes;
                for (final barcode in barcodes) {
                  final scannedValue = barcode.rawValue;
                  if (scannedValue != null && scannedValue.isNotEmpty) {
                    setState(() {
                      _partnerCodeController.text =
                          scannedValue.trim().toUpperCase().replaceAll('#', '');
                    });
                    Navigator.pop(ctx);
                    _linkPartner();
                    break;
                  }
                }
              },
            ),
            Positioned(
              top: 20,
              right: 20,
              child: IconButton(
                icon: const Icon(Icons.close_rounded, color: Colors.white, size: 28),
                onPressed: () => Navigator.pop(ctx),
              ),
            ),
            const Positioned(
              bottom: 30,
              left: 20,
              right: 20,
              child: Text(
                'Point camera at your partner\'s Duora QR code',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.white70, fontSize: 14),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);
    final myCode = authService.currentUser?.myPairCode ?? 'A9F2X1';

    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Partner Pairing',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Header
              const Text(
                'Connect Two Hearts Together',
                style: TextStyle(
                  color: DuoraColors.textPrimary,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Share your code or scan your partner’s QR code to link your private space.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: DuoraColors.textSecondary,
                  fontSize: 13,
                ),
              ),

              const SizedBox(height: 28),

              // QR Code Card
              Container(
                padding: const EdgeInsets.all(22),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: DuoraColors.primaryPink.withOpacity(0.25),
                      blurRadius: 28,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    QrImageView(
                      data: myCode,
                      version: QrVersions.auto,
                      size: 200.0,
                      eyeStyle: const QrEyeStyle(
                        eyeShape: QrEyeShape.square,
                        color: Color(0xFF0D0A14),
                      ),
                      dataModuleStyle: const QrDataModuleStyle(
                        dataModuleShape: QrDataModuleShape.square,
                        color: Color(0xFF0D0A14),
                      ),
                    ),
                    const SizedBox(height: 14),
                    Text(
                      '#$myCode',
                      style: const TextStyle(
                        color: Colors.black87,
                        fontSize: 24,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 4,
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 18),

              // Copy My Code Button
              TextButton.icon(
                icon: const Icon(Icons.copy_rounded, color: DuoraColors.primaryPink, size: 18),
                label: const Text(
                  'Copy My Pair Code',
                  style: TextStyle(
                    color: DuoraColors.primaryPink,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                onPressed: () {
                  Clipboard.setData(ClipboardData(text: myCode));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      backgroundColor: DuoraColors.surfaceCard,
                      content: Text('Your pair code copied to clipboard!'),
                    ),
                  );
                },
              ),

              const SizedBox(height: 24),
              const Divider(color: DuoraColors.divider),
              const SizedBox(height: 24),

              // Partner Code Input Section
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: DuoraColors.surfaceCard,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: DuoraColors.borderMuted),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Enter Partner\'s Code',
                      style: TextStyle(
                        color: DuoraColors.textPrimary,
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _partnerCodeController,
                            textCapitalization: TextCapitalization.characters,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              letterSpacing: 2,
                            ),
                            decoration: const InputDecoration(
                              hintText: 'e.g. X7K9P2',
                              prefixIcon: Icon(Icons.favorite_outline,
                                  color: DuoraColors.primaryPink),
                            ),
                          ),
                        ),
                        const SizedBox(width: 10),
                        // QR Scanner Button
                        Container(
                          height: 56,
                          width: 56,
                          decoration: BoxDecoration(
                            color: DuoraColors.surfaceHighlight,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: DuoraColors.borderMuted),
                          ),
                          child: IconButton(
                            icon: const Icon(Icons.qr_code_scanner_rounded,
                                color: DuoraColors.primaryPink, size: 26),
                            onPressed: _openQrScanner,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    ElevatedButton(
                      onPressed: _isPairing ? null : _linkPartner,
                      child: _isPairing
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.link_rounded, size: 20),
                                SizedBox(width: 8),
                                Text('Connect Hearts'),
                              ],
                            ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }
}
