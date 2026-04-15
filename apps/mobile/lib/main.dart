import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'providers/providers.dart';
import 'screens/issue_list_screen.dart';
import 'screens/login_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  bindPchSharedPreferences(await SharedPreferences.getInstance());
  runApp(
    ProviderScope(
      // Riverpod 3 기본 자동 재시도 비활성화(로그인·토큰 로드 등에서 무한 재시도 방지)
      retry: (retryCount, error) => null,
      child: const PchApp(),
    ),
  );
}

class PchApp extends ConsumerWidget {
  const PchApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authNotifierProvider);

    Widget home = auth.when(
      data: (token) =>
          token == null ? const LoginScreen() : const IssueListScreen(),
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (_, __) => const LoginScreen(),
    );

    return MaterialApp(
      title: 'Project Control Hub',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6366F1),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: home,
    );
  }
}
