import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../core/theme/duora_theme.dart';

class CoupleMemory {
  final String id;
  final String title;
  final String description;
  final String imageUrl;
  final DateTime date;
  final bool isFavorite;

  CoupleMemory({
    required this.id,
    required this.title,
    required this.description,
    required this.imageUrl,
    required this.date,
    this.isFavorite = false,
  });
}

class MemoriesScreen extends StatefulWidget {
  const MemoriesScreen({super.key});

  @override
  State<MemoriesScreen> createState() => _MemoriesScreenState();
}

class _MemoriesScreenState extends State<MemoriesScreen> {
  final List<CoupleMemory> _memories = [
    CoupleMemory(
      id: '1',
      title: 'Our First Sunset Walk 🌅',
      description: 'The golden hour by the riverbank. Time stood still for both of us.',
      imageUrl: 'https://images.unsplash.com/photo-1518199266791-5375a83190b7?q=80&w=600',
      date: DateTime.now().subtract(const Duration(days: 120)),
      isFavorite: true,
    ),
    CoupleMemory(
      id: '2',
      title: 'Stargazing Night ✨',
      description: 'Counting shooting stars and making endless wishes together.',
      imageUrl: 'https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=600',
      date: DateTime.now().subtract(const Duration(days: 45)),
      isFavorite: true,
    ),
  ];

  void _showAddMemoryDialog() {
    final titleCtrl = TextEditingController();
    final descCtrl = TextEditingController();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: DuoraColors.surfaceCard,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => Padding(
        padding: EdgeInsets.only(
          left: 20,
          right: 20,
          top: 20,
          bottom: MediaQuery.of(ctx).viewInsets.bottom + 20,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Add Couple Moment',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: titleCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Moment Title',
                hintText: 'e.g. Birthday Celebration',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: descCtrl,
              maxLines: 2,
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                labelText: 'Sweet caption or memory',
                hintText: 'What made this special?',
              ),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                if (titleCtrl.text.trim().isEmpty) return;
                setState(() {
                  _memories.insert(
                    0,
                    CoupleMemory(
                      id: DateTime.now().millisecondsSinceEpoch.toString(),
                      title: titleCtrl.text.trim(),
                      description: descCtrl.text.trim(),
                      imageUrl:
                          'https://images.unsplash.com/photo-1518199266791-5375a83190b7?q=80&w=600',
                      date: DateTime.now(),
                      isFavorite: true,
                    ),
                  );
                });
                Navigator.pop(ctx);
              },
              child: const Text('Save to Moments Vault'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat('MMMM dd, yyyy');

    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Our Moments & Vault',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: DuoraColors.primaryPink,
        foregroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        onPressed: _showAddMemoryDialog,
        child: const Icon(Icons.add_a_photo_rounded, size: 26),
      ),
      body: SafeArea(
        child: _memories.isEmpty
            ? const Center(
                child: Text(
                  'No memories yet. Tap + to add your first photo moment.',
                  style: TextStyle(color: DuoraColors.textMuted),
                ),
              )
            : ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                itemCount: _memories.length,
                itemBuilder: (context, index) {
                  final memory = _memories[index];
                  return Container(
                    margin: const EdgeInsets.symmetric(vertical: 10),
                    decoration: BoxDecoration(
                      color: DuoraColors.surfaceCard,
                      borderRadius: BorderRadius.circular(22),
                      border: Border.all(color: DuoraColors.borderMuted),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        ClipRRect(
                          borderRadius: const BorderRadius.vertical(top: Radius.circular(22)),
                          child: Image.network(
                            memory.imageUrl,
                            height: 180,
                            width: double.infinity,
                            fit: BoxFit.cover,
                            errorBuilder: (_, __, ___) => Container(
                              height: 180,
                              color: DuoraColors.surfaceHighlight,
                              child: const Icon(Icons.photo_rounded,
                                  color: DuoraColors.textMuted, size: 48),
                            ),
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    memory.title,
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  Text(
                                    dateFormat.format(memory.date),
                                    style: const TextStyle(
                                      color: DuoraColors.textMuted,
                                      fontSize: 11,
                                    ),
                                  ),
                                ],
                              ),
                              if (memory.description.isNotEmpty) ...[
                                const SizedBox(height: 6),
                                Text(
                                  memory.description,
                                  style: const TextStyle(
                                    color: DuoraColors.textSecondary,
                                    fontSize: 13,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
      ),
    );
  }
}
