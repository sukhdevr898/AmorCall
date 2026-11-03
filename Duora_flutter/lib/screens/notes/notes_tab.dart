import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';

class CoupleNoteItem {
  final String id;
  final String title;
  final String content;
  final String category;
  final DateTime date;
  final Color accentColor;

  CoupleNoteItem({
    required this.id,
    required this.title,
    required this.content,
    required this.category,
    required this.date,
    required this.accentColor,
  });
}

class NotesTab extends StatefulWidget {
  const NotesTab({super.key});

  @override
  State<NotesTab> createState() => _NotesTabState();
}

class _NotesTabState extends State<NotesTab> {
  final List<CoupleNoteItem> _notes = [
    CoupleNoteItem(
      id: '1',
      title: 'Our Next Trip Destination ✈️',
      content: 'Let\'s visit Manali or Bali for our anniversary! Don\'t forget to pack cozy jackets.',
      category: 'Bucket List',
      date: DateTime.now().subtract(const Duration(days: 2)),
      accentColor: const Color(0xFF38BDF8),
    ),
    CoupleNoteItem(
      id: '2',
      title: 'A Little Promise ❤️',
      content: 'No matter how busy our days get, we will always call each other before sleeping.',
      category: 'Promise',
      date: DateTime.now().subtract(const Duration(days: 5)),
      accentColor: DuoraColors.primaryPink,
    ),
  ];

  void _showAddNoteDialog() {
    final titleCtrl = TextEditingController();
    final contentCtrl = TextEditingController();
    String category = 'Love';

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: DuoraColors.surfaceCard,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModalState) => Padding(
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
                'New Shared Note',
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
                  labelText: 'Note Title',
                  hintText: 'e.g. Favorite Movies to Watch',
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: contentCtrl,
                maxLines: 3,
                style: const TextStyle(color: Colors.white),
                decoration: const InputDecoration(
                  labelText: 'Your thoughts & sweet words',
                  hintText: 'Write something heartfelt...',
                ),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  if (titleCtrl.text.trim().isEmpty) return;
                  setState(() {
                    _notes.insert(
                      0,
                      CoupleNoteItem(
                        id: DateTime.now().millisecondsSinceEpoch.toString(),
                        title: titleCtrl.text.trim(),
                        content: contentCtrl.text.trim(),
                        category: category,
                        date: DateTime.now(),
                        accentColor: DuoraColors.primaryPink,
                      ),
                    );
                  });
                  Navigator.pop(ctx);
                },
                child: const Text('Save Note'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Couple Notes & Vault',
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
        onPressed: _showAddNoteDialog,
        child: const Icon(Icons.add_rounded, size: 28),
      ),
      body: SafeArea(
        child: _notes.isEmpty
            ? const Center(
                child: Text(
                  'No shared notes yet. Tap + to write your first love note.',
                  style: TextStyle(color: DuoraColors.textMuted),
                ),
              )
            : ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                itemCount: _notes.length,
                itemBuilder: (context, index) {
                  final note = _notes[index];
                  return Container(
                    margin: const EdgeInsets.symmetric(vertical: 8),
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: DuoraColors.surfaceCard,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: note.accentColor.withOpacity(0.35)),
                      boxShadow: [
                        BoxShadow(
                          color: note.accentColor.withOpacity(0.08),
                          blurRadius: 12,
                        ),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 10, vertical: 3),
                              decoration: BoxDecoration(
                                color: note.accentColor.withOpacity(0.18),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                note.category,
                                style: TextStyle(
                                  color: note.accentColor,
                                  fontSize: 11,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete_outline_rounded,
                                  color: DuoraColors.textMuted, size: 18),
                              onPressed: () {
                                setState(() => _notes.removeAt(index));
                              },
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          note.title,
                          style: const TextStyle(
                            color: DuoraColors.textPrimary,
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          note.content,
                          style: const TextStyle(
                            color: DuoraColors.textSecondary,
                            fontSize: 13,
                            height: 1.4,
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
