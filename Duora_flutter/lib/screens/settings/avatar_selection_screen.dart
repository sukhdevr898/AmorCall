import 'package:flutter/material.dart';
import '../../core/theme/duora_theme.dart';

class AvatarSelectionScreen extends StatefulWidget {
  final String currentAvatarId;
  const AvatarSelectionScreen({super.key, this.currentAvatarId = 'boy_001'});

  @override
  State<AvatarSelectionScreen> createState() => _AvatarSelectionScreenState();
}

class _AvatarSelectionScreenState extends State<AvatarSelectionScreen> {
  late String _selectedAvatarId;

  final List<String> _boyAvatars = List.generate(
      10, (i) => 'https://api.dicebear.com/7.x/adventurer/png?seed=boy_${i + 1}');
  final List<String> _girlAvatars = List.generate(
      10, (i) => 'https://api.dicebear.com/7.x/adventurer/png?seed=girl_${i + 1}');

  int _selectedTab = 0;

  @override
  void initState() {
    super.initState();
    _selectedAvatarId = widget.currentAvatarId;
  }

  @override
  Widget build(BuildContext context) {
    final list = _selectedTab == 0 ? _boyAvatars : _girlAvatars;

    return Scaffold(
      backgroundColor: DuoraColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text(
          'Choose Couple Avatar',
          style: TextStyle(
            color: DuoraColors.textPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 20,
          ),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Gender / Category Switcher
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              child: Row(
                children: [
                  Expanded(
                    child: ChoiceChip(
                      label: const Center(child: Text('Him / Boys')),
                      selected: _selectedTab == 0,
                      selectedColor: DuoraColors.primaryPink,
                      backgroundColor: DuoraColors.surfaceCard,
                      labelStyle: TextStyle(
                        color: _selectedTab == 0 ? Colors.white : DuoraColors.textSecondary,
                        fontWeight: FontWeight.bold,
                      ),
                      side: const BorderSide(color: DuoraColors.borderMuted),
                      onSelected: (_) => setState(() => _selectedTab = 0),
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: ChoiceChip(
                      label: const Center(child: Text('Her / Girls')),
                      selected: _selectedTab == 1,
                      selectedColor: DuoraColors.primaryPink,
                      backgroundColor: DuoraColors.surfaceCard,
                      labelStyle: TextStyle(
                        color: _selectedTab == 1 ? Colors.white : DuoraColors.textSecondary,
                        fontWeight: FontWeight.bold,
                      ),
                      side: const BorderSide(color: DuoraColors.borderMuted),
                      onSelected: (_) => setState(() => _selectedTab = 1),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 10),

            // Avatars Grid
            Expanded(
              child: GridView.builder(
                padding: const EdgeInsets.all(20),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  crossAxisSpacing: 16,
                  mainAxisSpacing: 16,
                  childAspectRatio: 1.0,
                ),
                itemCount: list.length,
                itemBuilder: (context, index) {
                  final url = list[index];
                  final isSelected = _selectedAvatarId == url;

                  return GestureDetector(
                    onTap: () => setState(() => _selectedAvatarId = url),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: isSelected ? DuoraColors.primaryPink : DuoraColors.borderMuted,
                          width: isSelected ? 3 : 1.5,
                        ),
                        boxShadow: isSelected
                            ? [
                                BoxShadow(
                                  color: DuoraColors.primaryPink.withOpacity(0.5),
                                  blurRadius: 14,
                                ),
                              ]
                            : null,
                      ),
                      child: ClipOval(
                        child: Image.network(
                          url,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            color: DuoraColors.surfaceCard,
                            child: const Icon(Icons.person, color: Colors.white),
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),

            // Save Button
            Padding(
              padding: const EdgeInsets.all(20),
              child: ElevatedButton(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      backgroundColor: DuoraColors.surfaceCard,
                      content: Text('Avatar updated successfully!'),
                    ),
                  );
                  Navigator.pop(context, _selectedAvatarId);
                },
                child: const Text('Confirm Avatar'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
