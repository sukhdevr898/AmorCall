import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';

class PairingRequestDialog extends StatelessWidget {
  final String partnerName;
  final String partnerCode;
  final VoidCallback onAccept;
  final VoidCallback onDecline;

  const PairingRequestDialog({
    super.key,
    required this.partnerName,
    required this.partnerCode,
    required this.onAccept,
    required this.onDecline,
  });

  static Future<bool?> show(
    BuildContext context, {
    required String partnerName,
    required String partnerCode,
    required VoidCallback onAccept,
    required VoidCallback onDecline,
  }) {
    return showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => PairingRequestDialog(
        partnerName: partnerName,
        partnerCode: partnerCode,
        onAccept: () {
          Navigator.pop(ctx, true);
          onAccept();
        },
        onDecline: () {
          Navigator.pop(ctx, false);
          onDecline();
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.transparent,
      insetPadding: const EdgeInsets.symmetric(horizontal: 24),
      child: Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: DuoraColors.surfaceCard,
          borderRadius: BorderRadius.circular(28),
          border: Border.all(color: DuoraColors.primaryPink.withOpacity(0.4), width: 1.5),
          boxShadow: [
            BoxShadow(
              color: DuoraColors.primaryPink.withOpacity(0.25),
              blurRadius: 30,
              spreadRadius: 2,
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Glowing Heart Icon
            Container(
              width: 68,
              height: 68,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: DuoraColors.primaryGradient,
                boxShadow: [
                  BoxShadow(
                    color: DuoraColors.primaryPink.withOpacity(0.4),
                    blurRadius: 20,
                  ),
                ],
              ),
              child: const Center(
                child: Icon(Icons.favorite_rounded, color: Colors.white, size: 36),
              ),
            ),
            const SizedBox(height: 18),
            const Text(
              'Pairing Request Received! ❤️',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 10),
            Text(
              '$partnerName (#$partnerCode) has requested to link your Duora spaces. Once connected, your private hotline, chats, and moments will be shared exclusively.',
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: DuoraColors.textSecondary,
                fontSize: 13,
                height: 1.4,
              ),
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: DuoraColors.textMuted,
                      side: const BorderSide(color: DuoraColors.borderMuted),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                    onPressed: onDecline,
                    child: const Text('Decline'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: DuoraColors.primaryPink,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                    onPressed: onAccept,
                    child: const Text('Accept & Pair'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
